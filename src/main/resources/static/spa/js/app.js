import Dashboard from "./Dashboard.js";
import Settings from "./Settings.js";
import Tickets from "./Tickets.js";
import Requests from "./Requests.js";

const navigateTo = url => {
    history.pushState(null, null, url);
    router();
};


const router = async () => {
    const routes = [
        { path: "/app", view: Dashboard },
        { path: "/app/tickets", view: Tickets },
        { path: "/app/requests", view: Requests },
        { path: "/app/settings", view: Settings }
    ];

    // Test for location matches
    const potentialMatches = routes.map(route => {
        return {
            route: route,
            isMatch: location.pathname === route.path
        }
    });

    let match = potentialMatches.find( found => found.isMatch);
    if (!match) {
        return {
            route: routes[0],
            isMatch: true
        };
    }

    const view = new match.route.view();

    document.querySelector("#app").innerHTML = await view.getHtml();

};

window.addEventListener("popstate", router);

document.addEventListener("DOMContentLoaded", () => {
    document.body.addEventListener("click", e => {
        if ( e.target.matches("[data-link]")) {
            e.preventDefault();
            navigateTo(e.target.href);
        }
    });

    router();
});