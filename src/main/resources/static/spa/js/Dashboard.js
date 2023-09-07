import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Dashboard");
    }

    async getHtml() {
        let myItems = await getRemoteRequstTicketsByUser(this.params.token);
        let items = await getRemoteDashboard(this.params.token);
        let returnHTML = `<h1>Dashboard</h1>`;

        returnHTML += `<h1>My Open Items</h1>`;
        for (let rt in myItems) {
            let itemRT = myItems[rt];
            returnHTML += htmlItemLine(itemRT);
        }

        returnHTML += `<hr>`;
        for (let i in items) {
            let item = items[i];
            returnHTML += htmlItemLine(item);
        }

        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteRequstTicketsByUser(token) {
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