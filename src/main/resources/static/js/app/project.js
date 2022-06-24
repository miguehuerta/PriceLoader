app.project = () => {
	let action = app.getHash();

	console.log(action)
	if (action[1] === "list") {
		showProyects();
	}
	if (action[1] === "add") {
		addProyect();
	}
	if (action[1] === "edit") {
		editProject(action[2]);
	}

}

const updateExcelFiles = (_id) => {
	app.destroyPoppoverAndTooltip()
	app.get(`${app.api}/getExcelFilesByProjectId/${_id}`, (excel_files) => {
		let data = [];
		excel_files.map((excel_file) => {
			app.get(`${app.api}/getExcelValidationFileByExcelFileId/${excel_file.id}`, (validation_file) => {
				data.push(validation_file)
				app.get_promise(`${app.api}/getFileErrorLogsByExcelValidationFileId/${validation_file.id}`)
					.then(errors_log => {
						let errors = errors_log.map((error) => { return error.message })
						data.map((item) => {
							if (item.id === validation_file.id) {
								let errorsStr = errors.length > 0 ? errors.join('<hr>') : "Sin errores";
								item.errors = errorsStr
							}
						})

						app.destroyPoppoverAndTooltip()
						app.renderView("#excel_files", excelFilestemplate, { 'files': data });
						app.poppover();
						app.tooltips();
					})
			})
		})
	})
}

const editProject = (_id) => {
	app.get(`${app.api}/getProject/${_id}`, (data) => {
		console.log(data)
		app.renderView("#main-view", editProjectTemplate, data);
		app.onChange("formFileMultiple", function(target) {
			let filesObj = document.querySelector("#formFileMultiple").files;
			let files = Object.keys(filesObj).map(key => {
				let file = filesObj[key]
				let action = app.getHash();
				let id_project = action[2]
				app.uploadFiles(file, id_project)
					.then(response => {
						updateExcelFiles(_id);
						document.getElementById("formFileMultiple").value = ""
					})
			});

		})
		updateExcelFiles(_id);
	})

}

const showProyects = () => {
	app.get(`${app.api}/getProjectsByLoggedUserStepOne`, (data) => {
		console.log(data)
		app.renderView("#main-view", projectsTemplate, { 'data': data });
	})
}


const addProyect = () => {
	app.renderView("#main-view", addProjectTemplate, []);

	app.onSubmit("projectForm", (form) => {
		let formdata = app.getformdata(form)


		let immediateChangeState = document.getElementById("immediateChange").checked;
		let immediateChangeValue = immediateChangeState == true ? true : false;
		formdata.immediateChange = immediateChangeValue;
		if (delete formdata.files) {
			app.post(`${app.api}/saveProject`, formdata).then(data => {
				app.redirectTo("project/list")
			})
		}
	})

	applyConnfigurationsAddProject();
}

Date.prototype.today = function() {
	return this.getFullYear() + '-' + (((this.getMonth() + 1) < 10) ? "0" : "") + + (this.getMonth() + 1) + '-' + ((this.getDate() < 10) ? "0" : "") + this.getDate();
}
Date.prototype.timeNow = function() {
	return ((this.getHours() < 10) ? "0" : "") + this.getHours() + ":" + ((this.getMinutes() < 10) ? "0" : "") + this.getMinutes();
}

const applyDateInputConfigs = () => {
	let datetime = new Date().today() + "T" + new Date().timeNow();
	let dateControls = document.getElementsByClassName('datetime');
	if (dateControls.length > 0) {
		for (let i = 0; i < dateControls.length; i++) {
			let dateControl = dateControls[i];
			dateControl.value = datetime;
		}
	}

}

const applNullValuesDateInputConfigs = () => {
	let dateControls = document.getElementsByClassName('datetime');
	if (dateControls.length > 0) {
		for (let i = 0; i < dateControls.length; i++) {
			let dateControl = dateControls[i];
			dateControl.value = "";
		}
	}
}


const applyConnfigurationsAddProject = () => {

	applyDateInputConfigs()
	app.onChange("immediateChange", function(target) {
		if (target.srcElement.checked) {
			applNullValuesDateInputConfigs()
			let element = document.getElementById("dateTimeInputs");
			element.classList.add("d-none");
		} else {
			applyDateInputConfigs();
			let element = document.getElementById("dateTimeInputs");
			element.classList.remove("d-none");
		}
	})
}

const confirmDeleteProject = (_id, accept_message) => {
	data = { 'id': _id }
	app.delete(`${app.api}/deleteProject`, data)
		.then(response => {
			showProyects();
			alertify.success(accept_message)
		})
}

const deleteProject = (_id, name) => {
	let message = `Está a punto de elliminar el proyecto ${name}?`
	let answer = "¿Eliminar proyecto?"
	let cancel_message = "Acción cancelada"
	let file_deleted = "Proyecto fue eliminado"
	app.acceptModalMessage(message, answer, cancel_message, () => function() { return confirmDeleteProject(_id, file_deleted) })

}



const confirmDeleteExcelFile = (_id, accept_message) => {
	data = { 'id': _id }
	app.delete(`${app.api}/deleteExcelFile`, data)
		.then(response => {
			console.log(response)
			let action = app.getHash();
			let id_project = action[2]
			console.log(id_project)
			editProject(id_project)
			app.destroyPoppoverAndTooltip()
			alertify.success(accept_message)
		})
}

const deleteExcelFile = (_id, file) => {
	let message = `Está a punto de eliminar el archivo ${file}?`
	let answer = "¿Eliminar archivo?"
	let cancel_message = "Acción cancelada"
	let file_deleted = "Archivo fue eliminado"
	app.acceptModalMessage(message, answer, cancel_message, () => function() { return confirmDeleteExcelFile(_id, file_deleted) })

}

const excelFilestemplate = `
	{{#files}}
		<div class="col-lg-3">
			<div class="row">
				<div class="col-lg-12 text-end">
					<i data-bs-html="true" class="btn fa fa-trash fa-lg aria-hidden="true" onclick="deleteExcelFile({{excelFile.id}}, '{{excelFile.location}}')" data-bs-toggle="tooltip" data-bs-placement="bottom" title="Eliminar proyecto {{excelFile.location}}"></i>
				</div>
				<div class=" text-center">
					<i data-bs-container="body" data-bs-toggle="popover" data-bs-placement="bottom"  data-bs-content="{{#errors}} {{errors}} {{/errors}} {{^errors}} Sin error{{/errors}}" class="fa-solid fa-file-excel fa-10x {{#error}} text-danger {{/error}} {{^error}} text-success {{/error}}"></i>
					<p class="badge bg-light text-dark text-wrap"><span>{{excelFile.location}}</span></p>
				</div>
			</div>
		</div>
	{{/files}}
`

const editProjectTemplate = `
	<div class="row">
		<hr></hr>
		<div class="col-lg-6">
			<label class="title">Nombre del proyecto: </label><span>{{name}}</span><br>
			<label class="title">Cambio imediato: </label><span>{{#immediateChange}} Si {{/immediateChange}} {{^immediateChange}} No {{/immediateChange}}</span><br>
			
			{{^immediateChange}}
			<label class="title">Fecha de inicio de descuentos: </label><span>{{startDate}}</span><br>
			<label class="title">Fecha de inicio de descuentos: </label><span>{{endDate}}</span><br>
			{{/immediateChange}}
			
			<label class="title">Etapa: </label><span>{{step}}</span>
			
		</div>
		<div class="col-lg-6 text-end">
			<label class="title">Fecha de creación: </label><span>{{creationDate}}</span><br>
			<label class="title">Fecha de modificación: </label><span>{{updatedDate}}</span>
		</div>
	</div>
	<hr></hr>
	<div class="row">
		
		<div class="mb-3">
		  <label for="formFileMultiple" class="form-label">Agregar archivos al proyecto</label>
		  <input class="form-control" type="file" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel" id="formFileMultiple" multiple>
		</div>
		<h3>Archivos del proyecto</h3>
		<div class="row" id="excel_files">

		</div>
	</div>
`


const projectsTemplate = `
<div class="row">
	<a href="?app/project/add">agregar proyecto</a><br>
	
	{{#data}}
	<div class="col-lg-4">
		<div class="card">
			<div class="card-header">
			    <h5 class="card-title">{{name}}</h5>
			</div>
			<div class="card-body">  
			    <p class="card-text">autor: {{user.firstname}} {{user.lastname}}</p>
			    <p class="card-text">creado: {{creationDate}}</p>
			    <p class="card-text">ultima modificación: {{updatedDate}}</p>
			    <p class="card-text">etapa: {{step}}</p>
		  	</div>
		  	<div class="card-footer text-muted">
		  		<div class="row">
		  			<div class="col-lg-6">
			    		<a href="?app/project/edit/{{id}}" class="btn btn-success btn-sm">Agregar archivos</a>
			    	</div>
			    	<div class="col-lg-6 text-end">
			    		<button onclick="deleteProject({{id}}), '{{name}}'" class="btn btn-danger btn-sm">Borrar proyecto</button>
			    	</div>
	  			</div>
			</div>
		</div>
	</div>
	{{/data}}
</div>
`

const addProjectTemplate = `
<form id="projectForm">
  	<div class="mb-3">
    	<label for="name" class="form-label">Nombre del proyecto</label>
    	<input type="text" class="form-control" id="name" name="name" aria-describedby="emailHelp">
    	<div id="Help" class="form-text">Dale un nombre descriptivo al proyecto</div>
   	</div>
	<div class="mb-3 form-check form-switch">
	  	<input class="form-check-input" type="checkbox" id="immediateChange" role="switch" name="immediateChange" id="flexSwitchCheckDefault">
	  	<label class="form-check-label" for="flexSwitchCheckDefault">¿Los descuentos se aplican innmediatos?</label>
	</div>
	<div class="row" id="dateTimeInputs">
	<div class="mb-3 col-lg-6">
    	<label for="dateToApplyDiscount" class="form-label">Fecha de inicio</label>
    	<input type="datetime-local" class="form-control datetime" name="startDate" aria-describedby="emailHelp">
    	<div id="Help" class="form-text">Fecha de inicio en la que aplicará el descuento</div>
    </div>
    
    <div class="mb-3 col-lg-6">
    	<label for="dateToApplyDiscount" class="form-label">Fecha de fin</label>
    	<input type="datetime-local" class="form-control datetime" name="endDate" aria-describedby="emailHelp">
    	<div id="Help" class="form-text">Fecha de fin en la que aplicará el descuento</div>
    </div>
    </div>
  </div>
  <button type="submit" class="btn btn-primary">Guardar</button>
</form>
`