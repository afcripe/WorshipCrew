import Dashboard from "./Dashboard.js";
import Login from "./Login.js";
import Settings from "./Settings.js";
import Tickets from "./Tickets.js";
import TicketView from "./TicketView.js";
import Requests from "./Requests.js";
import RequestView from "./RequestView.js";
import SearchView from "./SearchView.js";

import { postLogin, renewToken } from "./Login.js";
import { toggleDetail } from "./TicketView.js";
import { imageDialog } from "./ImageView.js";
import { updateRequest, updateRequestItem, showRequestHistory } from "./RequestView.js";

let appTheme = "dark";
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
        { path: "/app/ticket/:id", view: TicketView },
        { path: "/app/requests", view: Requests },
        { path: "/app/request/:id", view: RequestView },
        { path: "/app/settings", view: Settings }
    ];

    // clea the path
    let cleanPath = location.pathname.split("?")[0];

    // force hide search
    toggleSearch(true);

    // force close popups
    hidePopForms();

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

const getLoggedIn = async () => {
    if (!token) {
        token = "";
        localStorage.setItem("token", "");
    }
    if (!username) {
        username = "";
        localStorage.setItem("username", "");
    }
    if (!firstName) {
        firstName = "";
        localStorage.setItem("firstName", "");
    }
    if (!lastName) {
        lastName = "";
        localStorage.setItem("lastName", "");
    }

    if (token && token !== "") {
        let auth = await renewToken(token);
        localStorage.setItem("token", auth.token);
        localStorage.setItem("firstName", auth.firstName);
        localStorage.setItem("lastName", auth.lastName);
        firstName = auth.firstName;
        lastName = auth.lastName;
        token = auth.token;
        isLoggedIn = auth.loggedIn;
    } else {
        localStorage.setItem("token", "");
        localStorage.setItem("firstName", "");
        localStorage.setItem("lastName", "");
        username = ""
        firstName = "";
        lastName = "";
        token = "";
        isLoggedIn = false;
    }
    router();
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
    isLoggedIn = auth.loggedIn;
    router();
}
const toggleSearch = (hide = false) => {
    let srch = document.querySelector(".nav__search");
    if (isLoggedIn) {
        if (srch.classList.contains("nav__search-hide") && !hide) {
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
    toggleSearch();
    navigateTo("/app/search/"+searchString);
}

const hidePopForms = () => {
    let pops = document.querySelectorAll(".form__popup");
    if (pops.length > 0) {
        for (let p in pops) {
            pops[p].remove();
        }
    }
}

const toggleTheme = () => {
    if (appTheme === "dark") {
        appTheme = "light";
        localStorage.setItem("appTheme", "light");
    } else {
        appTheme = "dark";
        localStorage.setItem("appTheme", "dark");
    }
    setAppTheme();
}

const setAppTheme = () => {
    if (!appTheme) {
        appTheme ="dark";
        localStorage.setItem("appTheme", "");
    }
    let pageBody = document.querySelector("body");
    if (appTheme === 'dark') {
        try {
            pageBody.classList.add("dark");
        } catch (e) {
            console.log("dark mode is enabled")
        }
    } else {
        try {
            pageBody.classList.remove("dark");
        } catch (e) {
            console.log("dark mode is not enabled")
        }
    }
}

const clearLocalStorage = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("firstName");
    localStorage.removeItem("lastName");
    localStorage.removeItem("lastName");
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
        if ( e.target.matches("[data-link-request]")) {
            let url = "/app/request/"+e.target.dataset.linkRequest;
            navigateTo(url);
        }
        if ( e.target.matches("[data-ticket-detail-toggle]")) {
            toggleDetail();
        }
        if ( e.target.matches("[data-nav-image]")) {
            imageDialog(e.target.dataset.navImage);
        }
        if ( e.target.matches("[data-settings-theme]")) {
            // toggleTheme(e.target.dataset.settingsTheme);
            toggleTheme(e.target.dataset.settingsTheme);
        }
        if ( e.target.matches("[data-settings-logout]")) {
            localStorage.setItem("token", "");
            localStorage.setItem("firstName", "");
            localStorage.setItem("lastName", "");
            localStorage.setItem("username", "");
            username = "";
            firstName = "";
            lastName = "";
            token = "";
            isLoggedIn = false;
            router();
        }
        if ( e.target.matches("[data-request]")) {
            e.preventDefault();
            updateRequest(e.target.dataset.request, token);
        }
        if ( e.target.matches("[data-form-request]")) {
            e.preventDefault();
            if (e.target.dataset.formRequest === "update") {
                console.log("Update Request");
            } else {
                console.log("Cancelled Request Edit");
            }
            document.querySelector(".form__popup").remove();
        }
        if ( e.target.matches("[data-request-item]")) {
            e.preventDefault();
            updateRequestItem(e.target.dataset.requestItem, token);
        }
        if ( e.target.matches("[data-form-request-item]")) {
            e.preventDefault();
            if (e.target.dataset.formRequestItem === "update") {
                console.log("Update Request Item");
            } else {
                console.log("Cancelled Item Edit");
            }
            document.querySelector(".form__popup").remove();
        }

        if ( e.target.matches("[data-request-history]")) {
            showRequestHistory(e.target.dataset.requestHistory, token);
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

    // clearLocalStorage();
    // try to load vars from storage
    token = localStorage.getItem("token");
    username = localStorage.getItem("username");
    firstName = localStorage.getItem("firstName");
    lastName = localStorage.getItem("lastName");
    appTheme = localStorage.getItem("appTheme");

    setAppTheme();

    getLoggedIn();
});