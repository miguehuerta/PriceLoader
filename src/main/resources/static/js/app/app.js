//console.log(app)

window.onload = function() {
	/* Add your logic here */

	locationHashChanged();

}

if ("onhashchange" in window) {
	console.log("¡El navegador soporta el evento hashchange!");
}


// manejador de acciones
function locationHashChanged() {
	let action = app.getHash();
	// console.log("cambio el navegador",app.getHash())
	if (action[0] === "project") {
		app.project()
	}

	if (action[0] === "console") {
		app.console()
	}

}