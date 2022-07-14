app.console = () => {
	displayConsole();
}

const displayConsole = () => {
	app.renderView("#main-view", mainTemplate)

	updateActiveProject();


	app.get(`${app.api}/getProjectsByLoggedUserStepOne`, (available_projects) => {
		let headers = ["Proyecto", "Propietario", "Ultima actualización"]
		let data = []
		data.headers = headers
		data.available_projects = available_projects
		app.renderView("#available_projects", availableProjectsToDployTemplate, data)
	})
}

const updateActiveProject = () => {
	app.get(`${app.api}/getActiveProject`, (data) => {
		console.log(data)
		if (data.length == 0 || data.length == 1) {
			app.renderView("#active_project", activeProjectTemplate, { active_project: data })

			if (data.length > 0 && data[0].step == 2) {
				if (data[0].processing == true) {
					let step_description = `Aplicando etapa ${data[0].step} de 4 (${data[0].state})`
					app.renderView("#controls", processingTemplate, { description: step_description, progress: data[0].processedPercentage })
					console.log(app.myInterval)
					if (!app.myInterval) {
						console.log("Activando interval")
						app.myInterval = setInterval(updateActiveProject, 5000);
						console.log(`interval es ${app.myInterval}`)
					}

				} else {
					if (app.myInterval) {
						clearInterval(app.myInterval);
					}
					app.renderView("#controls", controls_2, data[0])
				}
			}

			if (data.length > 0 && data[0].step == 3) {
				if (data[0].processing == true) {
					let step_description = `Aplicando etapa ${data[0].step} de 4 (Actualización de registros)`
					app.renderView("#controls", processingTemplate, { description: step_description })
				} else {
					if (app.myInterval) {
						clearInterval(app.myInterval);
					}
					app.renderView("#controls", controls_3, data[0])
				}
			}

		} else {
			alertify.alert("Sucedió un error, contacte al administrador")
		}
	})
}

const applyRefresh = (step_description) => {
	app.renderView("#controls", processingTemplate, { description: step_description })
	console.log("Activando interval")
	app.myInterval = setInterval(updateActiveProject, 5000);
	console.log(`interval es ${app.myInterval}`)

}

const nextStepValidation = (_id) => {
	applyRefresh(`Aplicando etapa 3 de 4 (validación y actualización de registros)`);
	data = { id: _id }
	app.post(`${app.api}/nextStepValidation`, data);
}

const nextStepFinish = (_id) => {
	data = { id: _id }
	app.post(`${app.api}/nextStepFinish`, data)
	.then( (response) => {
		console.log(response)
		updateActiveProject();
		displayConsole();
	});
	updateActiveProject();
	displayConsole();
}

const getValidations = async (_id) => {

	let files = await app.get_promise(`${app.api}/getExcelFilesByProjectId/${_id}`);
	let validationFiles = files.map(async file => {
		let id_file = file.id
		let validationFile = await app.get_promise(`${app.api}/getExcelValidationFileByExcelFileId/${id_file}`);
		return validationFile.error;
	})

	return new Promise((resolve, reject) => {
		Promise.all(validationFiles)
			.then(response => {
				found = response.find(el => el == true)
				resolve(found)
			})
	})

}

const addToExecutionArea = async (_id) => {
	getValidations(_id)
		.then(error => {
			if (error === true) {
				alertify.alert("El proyecto tiene archivos con errores, elimínalos o corrígelos antes de desplegarlo")
			} else {
				data = { id: _id }
				app.post(`${app.api}/addProjectToExecutionArea`, data)
					.then(response => {
						console.log("agregando")
						console.log(response)
						if (response.length > 0) {
							alertify.alert('No puede haber más de un proyecto en consola de ejecución');
						} else {
							displayConsole();
						}
					})
			}
		})
}

const outProjectFromExecutionArea = (_id) => {
	applyRefresh(`Retirando de area de ejecución)`);
	data = { id: _id }
	app.post(`${app.api}/outProjectFromExecutionArea`, data)
		.then(response => {
			console.log(response)
			displayConsole();
		})
}

const outProjectFromValidationArea = (_id) => {
	applyRefresh(`Retirando de área de validación)`);
	data = { id: _id }
	app.post(`${app.api}/outProjectFromValidationArea`, data)
		.then(response => {
			console.log(response)
			displayConsole();
		})
}

const mainTemplate = `
<div class="row">
	<div id="active_project" class="p-2">

	</div>
	<div id="available_projects" class="p-2">

	</div>
</div>
`

const activeProjectTemplate = `
<div class="bg-light border border-4 p-3">
	{{^active_project}}
		<h3>Sin proyectos en área de ejecución</h3>
	{{/active_project}}
	
	{{#active_project}}
		<div class="row">
			<div class="col-lg-6">
				<h3>Proyecto en ejecución</h3>
			</div>
			<div class="col-lg-6 text-end">
				Autor: <span class="fw-bold"> {{user.firstname}}</span>
			</div>
		</div>
		<div class="row">
			<hr>
			<div class="col-lg-6">
				Proyecto: <span class="fw-bold"> {{name}}</span><br>
				Inmediato: <span class="fw-bold">{{#immediateChange}}Si{{/immediateChange}}{{^immediateChange}}Si{{/immediateChange}}</span><br>
			</div>
			<div class="col-lg-6 text-end"">
				Creado: <span class="fw-bold"> {{creationDate}}</span><br>
				Ultima actualización: <span class="fw-bold"> {{updatedDate}}</span><br>
			</div>
		</div>
		<hr>
		<div id="controls">
		</div>
	{{/active_project}}
</div>
`

const processingTemplate = `
<p>{{{description}}}</p>
<div class="progress">
  	<div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-valuenow="progress" aria-valuemin="0" aria-valuemax="100" style="width: {{progress}}%"></div>
</div>
`

const controls_2 = `
	<button class="btn btn-dark btn-sm" onclick=nextStepValidation({{id}})>Avanzar a actualización</button>
	<button class="btn btn-light btn-sm border border-2" onclick=outProjectFromExecutionArea({{id}})>Sacar de consola de ejecución</button>
`

const controls_3 = `
	<p>{{{state}}}</p>
	<button class="btn btn-dark btn-sm" onclick=nextStepFinish({{id}})>Finalizar proyecto</button>
`

availableProjectsToDployTemplate = `

<h4>Proyectos disponibles para desplegar</h4>
	<table class="table table-striped table-hover" name="table" id="table">
		<thead>
		{{#headers}}
	      	<th scope="col">{{.}}</th>
	    {{/headers}}
	    	<th></th>
		</thead>
		<tbody>
			{{#available_projects}}
				<tr>
					<td>{{name}}</td>
					<td>{{user.firstname}} {{user.lastname}}</td>
					<td>{{updatedDate}}</td>
					<td><button class="btn btn-dark btn-sm" onclick="addToExecutionArea({{id}})">Agregar a consola de ejecución</button></td>
				</tr>
			{{/available_projects}}
		</tbody>
	</table>
`
