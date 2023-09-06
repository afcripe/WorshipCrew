import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Requests");
    }

    async getHtml() {
        let requests = await getRemoteRequests(this.params.token);
        let returnHTML = `<h1>Requests</h1>`;
        for (let i in requests) {
            let req = requests[i];
            returnHTML += htmlRequestLine(req);
        }
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteRequests(token) {
    const response = await fetch('/api/v1/app/requests', {
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
    r+=`<div class="item-id" data-link-request="`+req.id+`">Request: `+req.id+`</div>`;
    r+=`<div class="item-user" data-link-request="`+req.id+`">`+req.user.fullName+`</div>`;
    r+=`<div class="item-date" data-link-request="`+req.id+`">`+formatDate(req.requestDate)+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="item-detail" data-link-request="`+req.id+`">`+req.requestNote+`</div>`;
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