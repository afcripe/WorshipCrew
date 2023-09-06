import Dashboard from "./Dashboard.js";
import Login from "./Login.js";
import Settings from "./Settings.js";
import Tickets from "./Tickets.js";
import TicketView from "./TicketView.js";
import Requests from "./Requests.js";
import RequestView from "./RequestView.js";
import SearchView from "./SearchView.js";

import { postLogin } from "./Login.js";
import { toggleDetail } from "./TicketView.js";
import { imageDialog } from "./ImageView.js";


let username = "";
let firstName = "";
let lastName = "";
let token = "";
let isLoggedIn = false;

const navigateTo = url => {
    history.pushState(null, null, url);
    router();
};

const pathToRegEx = path => new RegExp("^" + path.replace(/\//g, "\\/").replace(/:\w+/g, "(.+)") + "$");

const getParams = match => {
    const values = match.result.slice(1);
    const keys = Array.from(match.route.path.matchAll(/:(\w+)/g)).map(result => result[1]);

    let params = Object.fromEntries(
        keys.map((key, i) => {
            return [key, values[i]];
        }));
    params['token'] = token;
    params['username'] = username;
    params['firstName'] = firstName;
    params['lastName'] = lastName;
    return params;
}

const router = async () => {
    const routes = [
        { path: "/app", view: Dashboard },
        { path: "/app/login", view: Login },
        { path: "/app/search/:id", view: SearchView },
        { path: "/app/tickets", view: Tickets },
        { path: "/app/tickets?", view: Tickets },
        { path: "/app/ticket/:id", view: TicketView },
        { path: "/app/requests", view: Requests },
        { path: "/app/settings", view: Settings },
        { path: "/app/request/:id", view: RequestView }
    ];

    let cleanPath = location.pathname.split("?")[0];

    // Test for location matches
    const potentialMatches = routes.map(route => {
        return {
            route: route,
            result: cleanPath.match(pathToRegEx(route.path))
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

const getLoggedIn = () => {
    username = localStorage.getItem("username");
    firstName = localStorage.getItem("firstName");
    lastName = localStorage.getItem("lastName");
    token = localStorage.getItem("token");
    if (token !== "") {
        isLoggedIn = true;
    }
}

// Form Handlers //
const doLogin = async (u, p) => {
    let auth = await postLogin(u, p);
    localStorage.setItem("token", auth.token);
    localStorage.setItem("firstName", auth.firstName);
    localStorage.setItem("lastName", auth.lastName);
    localStorage.setItem("username", u);
    username = u;
    firstName = auth.firstName;
    lastName = auth.lastName;
    token = auth.token;
    isLoggedIn = true;
    router();
}
const toggleSearch = () => {
    let srch = document.querySelector(".nav__search");
    if (isLoggedIn) {
        if (srch.classList.contains("nav__search-hide")) {
            srch.classList.remove("nav__search-hide");
            srch.querySelector("input").focus();
        } else {
            srch.classList.add("nav__search-hide");
        }
    }
}
const submitSearch = () => {
    let inputSearch = document.getElementById("searchInput");
    let searchString = inputSearch.value;
    inputSearch.value = "";
    navigateTo("/app/search/"+searchString);
}

window.addEventListener("popstate", router);

document.addEventListener("DOMContentLoaded", () => {
    document.body.addEventListener("click", e => {
        if ( e.target.matches("[data-search]")) {
            toggleSearch();
        }
        if ( e.target.matches("[data-nav-link]")) {
            let loc = "/app/"+e.target.dataset.navLink;
            navigateTo(loc);
        }
        if ( e.target.matches("[data-link-search]")) {
            const tgt = document.querySelector('[data-link-search]');
            let url = e.target.dataset.linkSearch;
            navigateTo(url);
        }
        if ( e.target.matches("[data-link-ticket]")) {
            let url = "/app/ticket/"+e.target.dataset.linkTicket;
            navigateTo(url);
        }
        if ( e.target.matches("[data-ticket-detail-toggle]")) {
            toggleDetail();
        }
        if ( e.target.matches("[data-nav-image]")) {
            imageDialog(e.target.dataset.navImage);
        }
        if ( e.target.matches("[data-settings-logout]")) {
            localStorage.setItem("token", "");
            localStorage.setItem("firstName", "");
            localStorage.setItem("lastName", "");
            localStorage.setItem("userName", "");
            username = "";
            firstName = "";
            lastName = "";
            token = "";
            isLoggedIn = false;
            router();
        }
        if ( e.target.matches("[data-form-submit]")) {
            e.preventDefault();
            const submittedForm = document.querySelector('[data-form-submit]');
            if (submittedForm.dataset.formSubmit === "formLogin") {
                let username = document.getElementById("formLogin-username").value;
                let password = document.getElementById("formLogin-password").value;
                doLogin(username, password);
            }
        }
    });

    document.getElementById("searchButton").addEventListener("click", () => {
        submitSearch();
    });

    getLoggedIn();
    router();
});