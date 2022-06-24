let app = {}
app.get = function(url, action) {
	fetch(url)
		.then(response => response.json())
		.then(data => action(data));
}

app.get_promise = async function(url) {
	let response = await fetch(url)
	return response.json()
}

function isJsonString(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

app.promiseAllGet = function(urls, action) {
	let fetchUrls = []
	urls.forEach(url => {
		fetchUrls.push(fetch(url));
	});

	Promise.all(fetchUrls).then(function(responses) {
		// Get a JSON object from each of the responses
		return Promise.all(responses.map(function(response) {
			//console.log(response)
			// return {"url":response.url,"items":response.json()};
			return response.json()
		}));
	}).then(function(data) {
		// Log the data to the console
		// You would do something with both sets of data here
		// console.log("urls",urls,fetchUrls)
		action(data);
		// console.log(data);
	}).catch(function(error) {
		// if there's an error, log it
		console.log(error);
	});
}
app.post = async function(url = '', data = {}) {
	let response = await fetch(url, {
		method: 'POST', // *GET, POST, PUT, DELETE, etc.
		mode: 'cors', // no-cors, *cors, same-origin
		cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
		credentials: 'same-origin', // include, *same-origin, omit
		headers: {
			'Content-Type': 'application/json'
			// 'Content-Type': 'application/x-www-form-urlencoded',
		},
		redirect: 'follow', // manual, *follow, error
		referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
		body: JSON.stringify(data) // body data type must match "Content-Type" header
	});
	return isJsonString(response) ? response.json() : response; // parses JSON response into native JavaScript objects
}

app.put = async function(url = '', data = {}) {
	let response = await fetch(url, {
		method: 'PUT', // *GET, POST, PUT, DELETE, etc.
		mode: 'cors', // no-cors, *cors, same-origin
		cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
		credentials: 'same-origin', // include, *same-origin, omit
		headers: {
			'Content-Type': 'application/json'
			// 'Content-Type': 'application/x-www-form-urlencoded',
		},
		redirect: 'follow', // manual, *follow, error
		referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
		body: JSON.stringify(data) // body data type must match "Content-Type" header
	});
	return isJsonString(response) ? response.json() : response; // parses JSON response into native JavaScript objects
}

app.delete = async function(url = '', data = {}) {
	let response = await fetch(url, {
		method: 'DELETE', // *GET, POST, PUT, DELETE, etc.
		mode: 'cors', // no-cors, *cors, same-origin
		cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
		credentials: 'same-origin', // include, *same-origin, omit
		headers: {
			'Content-Type': 'application/json'
			// 'Content-Type': 'application/x-www-form-urlencoded',
		},
		redirect: 'follow', // manual, *follow, error
		referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
		body: JSON.stringify(data) // body data type must match "Content-Type" header
	});
	return isJsonString(response) ? response.json() : response; // parses JSON response into native JavaScript objects
}

// check all images and add the responsive class
app.ConvertImageToResponsive = () => {

	document.querySelectorAll("img").forEach(item => {
		if (document.querySelectorAll("img").length > 0) {
			// item.classList.add("img-fluid")
			if (!item.classList.contains("img-fluid")) {
				item.classList.add("img-fluid")
			}
			console.log(item)
		}
	});

}

app.uploadFiles = (file, id_project) => {
	return new Promise((resolve, reject) => {
		console.log("loading file...", file.name)
		file.name = `${file.lastModified}_${file.name}`
		var data = new FormData();
		data.append("file", file);
		fetch(`${app.api}/addExcelFile/${id_project}`, {
			method: 'POST',
			body: data
		}).then(response => response.json())
			.then(result => {
				resolve(result)
			}).catch(error => reject(error));
	})


};

app.getHash = () => { return window.location.href.includes("?app/") ? window.location.href.split("?app/")[1].split("/") : "" }

app.insertAfter = function(referenceNode, newNode) {
	referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}


app.onclick = (div, action) => {

	document.getElementById(div).addEventListener('click', function(e) {
		action();
		e.stopPropagation()
		e.preventDefault();
		e.cancelBubble = true;
	});

}

app.getformdata = (form) => {


	// return  JSON.stringify(Object.fromEntries(new FormData(form.target))) 
	return Object.fromEntries(new FormData(form.target))
}

app.onSubmit = (divId, action) => {

	document.getElementById(divId).addEventListener('submit', function(e) {
		e.stopPropagation()
		e.preventDefault();
		e.cancelBubble = true;
		action(e);
		return false;
	});
}

app.onChange = (divId, action) => {

	document.getElementById(divId).addEventListener('change', function(e) {
		e.stopPropagation()
		e.preventDefault();
		e.cancelBubble = true;
		action(e);
		return false;
	});

}

app.upsertStorage = (name, value) => {
	if (localStorage.getItem(name) == null) {
		itemList = { products: [] };
		itemList.products.push(value)
		localStorage.setItem(name, JSON.stringify(itemList));
	} else {
		itemList = JSON.parse(localStorage.getItem(name));
		itemList.products.push(value)
		localStorage.setItem(name, JSON.stringify(itemList));
	}

}

app.updateStorage = (name, value) => {
	localStorage.setItem(name, JSON.stringify(value));
}

app.createDatatable = (queryselector = '#table') => {
	let table = new DataTable(queryselector, {});

}

app.getStorage = (name) => {
	return JSON.parse(localStorage.getItem(name))
}
// quita un objeto de cualquier arreglo 
app.removeLocalStorageItem = (index, arraySplice) => {
	//https://love2dev.com/blog/javascript-remove-from-array/
	for (var i = 0; i < arraySplice.length; i++) {

		if (i == index) {

			arraySplice.splice(i, 1);
		}

	}
	return arraySplice;
}

// genera una impresion con la información de un template y una fuente de datos.
// retorna el template, si se agrega como primer parametro null  solo retornara el template sin asignarlo a un objeto del dom
app.renderView = (querySelector = null, template, data) => {
	//const Mustache = require('mustache');
	let render = Mustache.render(template, data);
	if (querySelector != null) {
		document.querySelector(querySelector).innerHTML = render
	}
	return render

}

// funcion para revisar que un valor se encuentre instanciado
app.isset = (key) => {
	return typeof key === 'undefined'
}

app.redirectTo = (_path) => {
	window.location.replace(`${app.path}/?app/${_path}`)
}


app.sortJsonArrayDesc = (arrayList, property) => {


	return arrayList.sort(app.sortByPropertyDesc(property));

}

// se modifica el algoritmo para funcionar de manera que ordene de menor a mayor, 
//en el futuro se puede hacer una funcion que ordene de diferentes maneras
app.sortByPropertyAsc = (property) => {
	return function(a, b) {
		if (a[property] > b[property])
			return 1;
		else if (a[property] < b[property])
			return -1;

		return 0;
	}
}


app.sortJsonArrayAsc = (arrayList, property) => {


	return arrayList.sort(app.sortByPropertyAsc(property));

}

app.destroyPoppoverAndTooltip = () => {
	let allTooltipsActive = document.getElementsByClassName("bs-tooltip-bottom");
	var tooltips_array = [...allTooltipsActive]; // converts NodeList to Array
	tooltips_array.forEach(tooltip => {
		tooltip.remove()
	});

	let allPopoversActive = document.getElementsByClassName("bs-popover-bottom");
	var popovers_array = [...allPopoversActive]; // converts NodeList to Array
	popovers_array.forEach(popover => {
		popover.remove()
	});

}

app.poppover = () => {
	var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
	var popoverList = popoverTriggerList.map(function(popoverTriggerEl) {
		return new bootstrap.Popover(popoverTriggerEl, { html: true, placement: 'bottom', container: 'body' })
	})
}

app.tooltips = () => {
	var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
	var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
		return new bootstrap.Tooltip(tooltipTriggerEl)
	})
}

app.acceptModalMessage = (answer, accept_message, cancel_message, callback) => {
    alertify.confirm(answer, accept_message,
        callback(),
        () => alertify.message(cancel_message));

}
