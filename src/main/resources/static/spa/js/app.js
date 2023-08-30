import Dashboard from "./Dashboard.js";
import Login from "./Login.js";
import Settings from "./Settings.js";
import Tickets from "./Tickets.js";
import TicketView from "./TicketView.js";
import Requests from "./Requests.js";
import RequestView from "./RequestView.js";


let username = "";
let firstName = "";
let isLoggedIn = false;

const navigateTo = url => {
    history.pushState(null, null, url);
    router();
};

const pathToRegEx = path => new RegExp("^" + path.replace(/\//g, "\\/").replace(/:\w+/g, "(.+)") + "$");

const getParams = match => {
    const values = match.result.slice(1);
    const keys = Array.from(match.route.path.matchAll(/:(\w+)/g)).map(result => result[1]);

    return  Object.fromEntries(
        keys.map((key, i) => {
            return [key, values[i]];
        }));
}

const router = async () => {
    const routes = [
        { path: "/app", view: Dashboard },
        { path: "/app/login", view: Login },
        { path: "/app/tickets", view: Tickets },
        { path: "/app/ticket/:id", view: TicketView },
        { path: "/app/requests", view: Requests },
        { path: "/app/settings", view: Settings },
        { path: "/app/request/:id", view: RequestView }
    ];

    // Test for location matches
    const potentialMatches = routes.map(route => {
        return {
            route: route,
            result: location.pathname.match(pathToRegEx(route.path))
        }
    });
    let match = potentialMatches.find( found => found.result !== null );

    if (!match) {
        let defaultPath = "/app";
        // Get default match if pathname
        const homeMatches = routes.map(route => {
            return {
                route: route,
                result: defaultPath.match(pathToRegEx(route.path))
            }
        });
        match = homeMatches.find( found => found.result !== null );
    }
    if (!isLoggedIn) {
        let loginPath = "/app/login";
        // Get match if not found or not logged in
        const loginMatches = routes.map(route => {
            return {
                route: route,
                result: loginPath.match(pathToRegEx(route.path))
            }
        });
        match = loginMatches.find( found => found.result !== null );
    }

    const view = new match.route.view(getParams(match));

    document.querySelector("#app").innerHTML = await view.getHtml();

};

const getLoggedIn = async () => {
    isLoggedIn =localStorage.getItem("isLoggedIn");
    if (isLoggedIn === null) { isLoggedIn = true; }
}


window.addEventListener("popstate", router);

document.addEventListener("DOMContentLoaded", () => {
    document.body.addEventListener("click", e => {
        if ( e.target.matches("[data-link]")) {
            e.preventDefault();
            navigateTo(e.target.href);
        }
    });
    getLoggedIn();
    router();
});