import Dashboard from "./Dashboard.js";
import Login from "./Login.js";
import Settings from "./Settings.js";
import Tickets from "./Tickets.js";
import TicketView from "./TicketView.js";
import Requests from "./Requests.js";
import RequestView from "./RequestView.js";
import SearchView from "./SearchView.js";

import { postLogin, renewToken } from "./Login.js";
import { toggleDetail, showTicketAgents, updateAgent, updateNote, updateTicketStatus, postTicketNote, postTicketStatus, postTicketAddAgent, postTicketRemoveAgent, updateTicketSLA, postTicketSLA } from "./TicketView.js";
import { imageDialog } from "./ImageView.js";
import { updateRequest, updateRequestItem, showRequestHistory, updateSupervisor, updateRequestAgent, updateRequestItemAgent, postRequestStatus, postRequestItemStatus, postRequestAddAgent, postRequestItemAddAgent, postRequestAddSupervisor, postRequestRemoveSupervisor } from "./RequestView.js";
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.3.1/firebase-app.js";
import { getMessaging, getToken, onMessage } from "https://www.gstatic.com/firebasejs/10.3.1/firebase-messaging.js";

let appTheme = "dark";
let username = "";
let firstName = "";
let lastName = "";
let token = "";
let isLoggedIn = false;

let filesToUpload;

const appProgress = document.getElementById("appProgress");

// FirebaseConfig
const firebaseConfig = {
    apiKey: "AIzaSyBws-XgELWSv0q_T6aEbngn9n8sGbjO2TI",
    authDomain: "destinyworshipexchange.firebaseapp.com",
    projectId: "destinyworshipexchange",
    storageBucket: "destinyworshipexchange.appspot.com",
    messagingSenderId: "395754529346",
    appId: "1:395754529346:web:a4f5652adb957aa04643db",
    measurementId: "G-NV2FY9N210"
};

const updateAppProgress = (val) => {
    try {
        if (val < 0) {
            document.getElementById("appProgress").value = 1;
            document.getElementById("appProgress").style.display = "none";
        } else if (val > 100) {
            document.getElementById("appProgress").value = 100;
            document.getElementById("appProgress").style.display = "none";
        } else if (val === 0) {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").removeAttribute("value");
        } else {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").value = val;
        }
    } catch (e) {
        document.getElementById("appProgress").style.display = "none";
    }
}

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

    updateAppProgress(1);

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

    updateAppProgress(101);

    await view.getNotification(token, username);
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
    updateAppProgress(1);
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
    updateAppProgress(101);
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
    updateAppProgress(1);
    let inputSearch = document.getElementById("searchInput");
    let searchString = inputSearch.value;
    inputSearch.value = "";
    toggleSearch();
    updateAppProgress(101);
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

const postTicketImageFile = async (ticketId) => {
    updateAppProgress(0);
    let filebrowser = document.getElementById("imageFile");
    filesToUpload = filebrowser.files.length;

    let formData = new FormData();
    for (let i=0; i < filebrowser.files.length; i++) {
        formData.set("imageFile", document.getElementById("imageFile").files[i]);

        const response = await fetch('/api/v1/app/ticket/uploadticketimage', {
            method: 'POST',
            body: formData
        }).then(response => {
            return response.json();
        }).then(data => {
            let imageDiv = document.createElement("div");
            imageDiv.classList.add("ticket__note-images");
            imageDiv.id = "img-"+data.id;
            imageDiv.addEventListener("click", function (event) {
                removeTicketImage(data.id)
            });

            let imageImg = document.createElement("img");
            imageImg.classList.add("selectable-image");
            imageImg.src = data.fileLocation;
            imageImg.alt = data.fileLocation;

            let rmDiv = document.createElement("div");
            rmDiv.innerText = "X";
            rmDiv.classList.add("removable-image");

            imageDiv.appendChild(imageImg);
            imageDiv.appendChild(rmDiv);
            document.getElementById("imagePath").appendChild(imageDiv);
            filesToUpload--;
            completeTicketImageUpload();
        });
    }
}

const completeTicketImageUpload = () => {
    if (filesToUpload === 0) {
        let filebrowser = document.getElementById("imageFile");
        while (filebrowser.firstChild) {
            filebrowser.removeChild(filebrowser.firstChild);
        }
    }
    updateAppProgress(101);
}

const removeTicketImage = async (id) => {
    updateAppProgress(1);
    let eleId = "img-"+id;
    let imgTag = document.getElementById(eleId);

    let formData = new FormData();
    formData.set("id", id);

    const response = await fetch('/api/v1/app/ticket/removeticketimage', {
        method: 'POST',
        body: formData
    }).then(response => {
        return response.json();
    }).then(data => {
        imgTag.remove();
        updateAppProgress(101);
    });
}

const postNewTicketNote = async (ticketId) => {
    updateAppProgress(1);
    let response = await postTicketNote(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/ticket/"+ticketId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewTicketStatus = async (ticketId) => {
    updateAppProgress(1);
    let response = await postTicketStatus(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/ticket/"+ticketId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewTicketSLA = async (ticketId) => {
    updateAppProgress(1);
    let response = await postTicketSLA(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/ticket/"+ticketId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewTicketAgent = async (ticketId) => {
    updateAppProgress(1);
    let response = await postTicketAddAgent(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/ticket/"+ticketId;
    updateAppProgress(101);
    navigateTo(url);
}

const postRemoveTicketAgent = async (ticketId, userId) => {
    updateAppProgress(0);
    let rsp = await postTicketRemoveAgent(ticketId, userId, token);
    updateAppProgress(101);
}

const postNewRequestStatus = async (requestId) => {
    updateAppProgress(1);
    let response = await postRequestStatus(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/request/"+requestId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewRequestItemStatus = async (requestId) => {
    updateAppProgress(1);
    let response = await postRequestItemStatus(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/request/"+requestId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewRequestAgent = async (requestId) => {
    updateAppProgress(1);
    let response = await postRequestAddAgent(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/request/"+requestId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewRequestItemAgent = async (requestId) => {
    updateAppProgress(1);
    let response = await postRequestItemAddAgent(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/request/"+requestId;
    updateAppProgress(101);
    navigateTo(url);
}

const postNewRequestSupervisor = async (requestId) => {
    updateAppProgress(1);
    let response = await postRequestAddSupervisor(token);
    document.querySelector(".form__popup").remove();
    let url = "/app/request/"+requestId;
    updateAppProgress(101);
    navigateTo(url);
}

const postRemoveRequestAgent = async (requestId, userId) => {
    updateAppProgress(0);
    let response = await postRequestRemoveSupervisor(requestId, userId, token);
    updateAppProgress(101);
}


// App Theming //
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

const browserInfo = () => {
    let thisOs = "Unknown OS";
    let thisBsr = "Unknown Browser";
    if (navigator.userAgent.indexOf("Win") !== -1) thisOs = "Windows";
    if (navigator.userAgent.indexOf("Mac") !== -1) thisOs = "Mac";
    if (navigator.userAgent.indexOf("Linux") !== -1) thisOs = "Linux";
    if (navigator.userAgent.indexOf("Android") !== -1) thisOs = "Android";
    if (navigator.userAgent.indexOf("iPhone") !== -1) thisOs = "iPhone";
    if (navigator.userAgent.indexOf("iPad") !== -1) thisOs = "iPad";

    if (navigator.userAgent.indexOf("Chrome") !== -1) thisBsr = "Chrome";
    if (navigator.userAgent.indexOf("Safari") !== -1) thisBsr = "Safari";
    if (navigator.userAgent.indexOf("Edg") !== -1) thisBsr = "Edge";
    if (navigator.userAgent.indexOf("Firefox") !== -1) thisBsr = "Firefox";
    if (navigator.userAgent.indexOf("OPR") !== -1) thisBsr = "Opera";

    return thisOs+" - "+thisBsr;
}

const subscribe = async() => {
    let regSW = await navigator.serviceWorker.register('/firebase-messaging-sw.js')

    console.log("subscribe requested");
    // Add the public key generated from the console here.
    getToken(messaging, {vapidKey: "BPkHKoGBXYuuTEfyty0lBzi1RruJbGobRImxy9Jl008QPmgNxeo7Hj2BYaDb-AJD4hOraF6ZHirFl_VtxeMKiZk"})
        .then(async (currentToken) => {
            console.log("Got token.")
            if (currentToken) {
                // Send token to server
                // navigator.serviceWorker.register("/firebase-messaging-sw.js");
                let appInst = browserInfo();
                let formData = new FormData();
                    formData.set("id", "0");
                    formData.set("name", appInst);
                    formData.set("token", currentToken);

                console.log("Sending token to server");
                const response = await fetch('/api/v1/app/swtoken', {
                    method: 'POST',
                    headers: {
                        authorization: "Bearer " + token
                    },
                    body: formData
                });
                let rsp = await response.json();
                console.log(rsp.name);
                navigateTo("/app/settings");
            } else {
                console.log("Needs browser permission");
                // Show permission request UI
                Notification.requestPermission()
                    .then((perm) => {
                        if (perm === 'granted') {
                            console.log("Permission Granted");
                        } else {
                            console.log("Permission Denied");
                        }
                    });
            }
        })
        .catch((err) => {
            console.log("Error getting token.")
            console.log(err);
        });
};

const unsubscribe = () =>{
    getToken(messaging, {vapidKey: "BPkHKoGBXYuuTEfyty0lBzi1RruJbGobRImxy9Jl008QPmgNxeo7Hj2BYaDb-AJD4hOraF6ZHirFl_VtxeMKiZk"})
        .then(async (currentToken) => {
            if (currentToken) {
                // Send token to server
                // navigator.serviceWorker.register("/firebase-messaging-sw.js");
                let appInst = browserInfo();
                let formData = new FormData();
                formData.set("id", "0");
                formData.set("name", appInst);
                formData.set("token", currentToken);

                const response = await fetch('/api/v1/app/removetoken', {
                    method: 'POST',
                    headers: {
                        authorization: "Bearer " + token
                    },
                    body: formData
                });
                let rsp = await response.json();
                console.log(rsp.name);
            }

            let regs = await navigator.serviceWorker.getRegistrations();
            for(let reg of regs) {
                await reg.unregister();
            }
            navigateTo("/app/settings");
        })
        .catch((err) => {
            console.log(err);
        });
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Firebase Cloud Messaging and get a reference to the service
const messaging = getMessaging(app);

// Message Handling
onMessage((payload) => {
    console.log("Message Received");
    Notification.requestPermission()
        .then(perm => {
            if (perm === 'granted') {
                let notify = new Notification(
                    "Notification",
                    {
                        body: "New Notification Received"
                    }
                );
            }
        });
});

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
        if ( e.target.matches("[data-ticket-agents]")) {
            showTicketAgents(e.target.dataset.ticketAgents, token);
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

        // Requests

        if ( e.target.matches("[data-request-status]")) {
            e.preventDefault();
            updateRequest(e.target.dataset.requestStatus, token);
        }
        if ( e.target.matches("[data-form-request-status]")) {
            e.preventDefault();
            if (e.target.dataset.formRequestStatus === "update") {
                postNewRequestStatus(document.getElementById("requestStatusId").value);
            } else {
                console.log("Cancelled Request Edit");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-request-agent]")) {
            e.preventDefault();
            updateRequestAgent(e.target.dataset.requestAgent, token);
        }
        if ( e.target.matches("[data-form-request-agent]")) {
            e.preventDefault();
            if (e.target.dataset.formRequestAgent === "update") {
                postNewRequestAgent(document.getElementById("requestAgentId").value);
            } else {
                console.log("Cancelled Request Include");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-request-item-status]")) {
            e.preventDefault();
            updateRequestItem(e.target.dataset.requestItemStatus, token);
        }
        if ( e.target.matches("[data-form-request-item-status]")) {
            e.preventDefault();
            if (e.target.dataset.formRequestItemStatus === "update") {
                postNewRequestItemStatus(document.getElementById("requestItemStatusRequestId").value);
            } else {
                console.log("Cancelled Item Edit");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-request-item-agent]")) {
            e.preventDefault();
            updateRequestItemAgent(e.target.dataset.requestItemAgent, token);
        }
        if ( e.target.matches("[data-form-request-item-agent]")) {
            e.preventDefault();
            if (e.target.dataset.formRequestItemAgent === "update") {
                postNewRequestItemAgent(document.getElementById("requestAgentAddId").value);
            } else {
                console.log("Cancelled Item Assigned");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-request-supervisor]")) {
            e.preventDefault();
            updateSupervisor(e.target.dataset.requestSupervisor, token);
        }
        if ( e.target.matches("[data-form-request-supervisor]")) {
            e.preventDefault();
            if (e.target.dataset.formRequestSupervisor === "update") {
                postNewRequestSupervisor(document.getElementById("requestSuperId").value);
            } else {
                console.log("Cancelled Supervisor");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-request-supervisor-remove]")) {
            e.preventDefault();
            postRemoveRequestAgent(e.target.dataset.requestId, e.target.dataset.requestSupervisorRemove)
            console.log("Remove Supervisor "+e.target.dataset.requestSupervisorRemove+" from "+e.target.dataset.requestId);
        }

        if ( e.target.matches("[data-request-history]")) {
            showRequestHistory(e.target.dataset.requestHistory, token);
        }

        // Tickets

        if ( e.target.matches("[data-ticket-agent-remove]")) {
            e.preventDefault();
            postRemoveTicketAgent(e.target.dataset.ticketId, e.target.dataset.ticketAgentRemove)
        }

        if ( e.target.matches("[data-ticket-agent]")) {
            e.preventDefault();
            document.getElementById("agentViewer").remove();
            updateAgent(e.target.dataset.ticketAgent, token);
        }
        if ( e.target.matches("[data-form-ticket-agent]")) {
            e.preventDefault();
            if (e.target.dataset.formTicketAgent === "update") {
                postNewTicketAgent(document.getElementById("ticketAgentId").value)
            } else {
                console.log("Cancelled Agent");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-ticket-status]")) {
            e.preventDefault();
            updateTicketStatus(e.target.dataset.ticketStatus, token);
        }
        if ( e.target.matches("[data-form-ticket-status]")) {
            e.preventDefault();
            if (e.target.dataset.formTicketStatus === "update") {
                postNewTicketStatus(document.getElementById("ticketStatusId").value);
            } else {
                console.log("Cancelled ticket status");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-ticket-note]")) {
            e.preventDefault();
            updateNote(e.target.dataset.ticketNote, token);
        }
        if ( e.target.matches("[data-form-ticket-note]")) {
            e.preventDefault();
            if (e.target.dataset.formTicketNote === "update") {
                postNewTicketNote(document.getElementById("ticketNoteId").value);
            } else {
                console.log("Cancelled New Note");
                document.querySelector(".form__popup").remove();
            }
        }

        if ( e.target.matches("[data-ticket-sla]")) {
            e.preventDefault();
            updateTicketSLA(e.target.dataset.ticketSla, token);
        }
        if ( e.target.matches("[data-form-ticket-sla]")) {
            e.preventDefault();
            if (e.target.dataset.formTicketSla === "update") {
                postNewTicketSLA(document.getElementById("ticketSLAId").value);
            } else {
                console.log("Cancelled Update SLA");
                document.querySelector(".form__popup").remove();
            }
        }

        // settings

        if ( e.target.matches("[data-settings-notify-true]")) {
            e.preventDefault();
            subscribe();
        }
        if ( e.target.matches("[data-settings-notify-false]")) {
            e.preventDefault();
            unsubscribe();
        }

        // login

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

    document.body.addEventListener("change", e => {
        if ( e.target.matches("[data-ticket-image-upload]")) {
            postTicketImageFile(e.target.dataset.ticketImageUpload);
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
