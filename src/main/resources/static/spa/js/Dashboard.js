import AbstractView from "./AbstractView.js";

export default class DashboardClass extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Dashboard");
    }

    async getHtml() {
        this.setAppProgress(20);
        let myItems = await getRemoteRequestTicketsByUser(this.params.token);
        let items = await getRemoteDashboard(this.params.token);
        let returnHTML = `<h1>Dashboard</h1>`;

        this.setAppProgress(40);
        returnHTML += `<h1>My Open Items</h1>`;
        for (let rt in myItems) {
            let itemRT = myItems[rt];
            returnHTML += htmlItemLine(itemRT);
        }

        this.setAppProgress(60);
        returnHTML += `<hr>`;
        for (let i in items) {
            let item = items[i];
            returnHTML += htmlItemLine(item);
        }

        this.setAppProgress(80);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }

    async getNotification() {
        return null;
    }
}

function setAppProgress(prg) {
    try {
        if (prg < 0) {
            document.getElementById("appProgress").value = 1;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg > 100) {
            document.getElementById("appProgress").value = 100;
            document.getElementById("appProgress").style.display = "none";
        } else if (prg === 0) {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").removeAttribute("value");
        } else {
            document.getElementById("appProgress").style.display = "block";
            document.getElementById("appProgress").value = prg;
        }
    } catch (e) {
        document.getElementById("appProgress").style.display = "none";
    }
}

async function getRemoteRequestTicketsByUser(token) {
    const response = await fetch('/api/v1/app/dashboarduseritems', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

async function getRemoteDashboard(token) {
    const response = await fetch('/api/v1/app/dashboard', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

function htmlItemLine(item) {
    let r = "";
    r+=`<div class="appList__item" data-nav-link="`+item.module+`">`;

    r+=`<div class="appList__item-line" data-nav-link="`+item.module+`">`;
    r+=`<div class="appList__item-id" data-nav-link="`+item.module+`">`+item.name+`</div>`;
    r+=`<div class="appList__item-count" data-nav-link="`+item.module+`">Items: `+item.itemCount+`</div>`;
    r+=`</div>`;

    r+=`<div class="appList__item-line" data-nav-link="`+item.module+`">`;
    r+=`<div class="appList__item-detail" data-nav-link="`+item.module+`">`+item.detail+`</div>`;
    r+=`</div>`;

    r+=`</div>`;

    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}