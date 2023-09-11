import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Requests");
    }

    async getHtml() {
        let myRequests = await getRemoteUserRequests(this.params.token);
        let requests = await getRemoteRequests(this.params.token);
        let requestItems = await getRemoteRequestItems(this.params.token);
        let returnHTML = `<h1>Requests</h1>`;

        if (myRequests.length > 0) {
            returnHTML += `<h2>My Requests</h2>`;
            for (let m in myRequests) {
                let mr = myRequests[m];
                returnHTML += htmlRequestLine(mr);
            }
        }

        if (requestItems.length > 0) {
            returnHTML += `<h2>Items to Fulfill</h2>`;
            for (let i in requestItems) {
                let itm = requestItems[i];
                returnHTML += htmlItemLine(itm);
            }
        }

        if (requests.length > 0) {
            returnHTML += `<h2>Requests</h2>`;
            for (let r in requests) {
                let req = requests[r];
                returnHTML += htmlRequestLine(req);
            }
        }

        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteUserRequests(token) {
    const response = await fetch('/api/v1/app/request/listbyuser', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

async function getRemoteRequests(token) {
    const response = await fetch('/api/v1/app/request/listbysupervisor', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

async function getRemoteRequestItems(token) {
    const response = await fetch('/api/v1/app/request/listitems', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const tickets = await response.json();
    const status = response.status;
    return tickets;
}

function htmlRequestLine(req) {
    let r = "";
    r+=`<div class="list__item" data-link-request="`+req.id+`">`;
    r+=`<div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="appList__item-id" data-link-request="`+req.id+`">Request: `+req.id+`</div>`;
    r+=`<div class="appList__item-name" data-link-request="`+req.id+`">`+req.user+`</div>`;
    r+=`<div class="appList__item-right" data-link-request="`+req.id+`">`+formatDate(req.date)+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="appList__item-detail" data-link-request="`+req.id+`">`+req.detail+`</div>`;
    r+=`<div class="appList__item-right" data-link-request="`+req.id+`">Items: `+req.itemCount+`</div>`;
    r+=`</div></div>`;

    return r;
}

function htmlItemLine(req) {
    let r = "";
    r+=`<div class="list__item" data-link-request="`+req.id+`">`;
    r+=`<div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="appList__item-id" data-link-request="`+req.id+`">`+req.name+`</div>`;
    r+=`<div class="appList__item-right" data-link-request="`+req.id+`">`+formatDate(req.date)+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="appList__item-detail" data-link-request="`+req.id+`">`+req.detail+`</div>`;
    r+=`<div class="appList__item-right" data-link-request="`+req.id+`">Items: `+req.itemCount+`</div>`;
    r+=`</div></div>`;

    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}