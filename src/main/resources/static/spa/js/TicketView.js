import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Ticket");
    }

    async getHtml() {
        let ticket = await getRemoteTicket(this.params.id);
        let returnHTML = htmlTicket(ticket);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteTicket(id) {
    const response = await fetch('/api/v1/app/ticket/'+id);
    const ticket = await response.json();
    return ticket;
}

function htmlTicket(tkt) {
    let r = `<div class="ticket__group">`;
    r+=`<div class="ticket__left ticket__title">Ticket: `+tkt.id+`</div>`;
    r+=`<div class="ticket__right ticket__title">`+tkt.ticketStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="ticket__detail">`+tkt.ticketDetail+`</div>`;

    r+=`<div class="ticket__detail-group">`;
    r+=`<i id="btnDetailExpand" class="bi bi-arrows-expand ticket__detail-expand"></i>`;
    r+=`<div class="ticket__detail">Date Due: `+formatDate(tkt.ticketDue)+`</div>`;
    r+=`<div class="ticket__detail">Date Sbmitted: `+formatDate(tkt.ticketDate)+`</div>`;
    if (tkt.closeDate) {
        r += `<div class="ticket__detail">` + formatDate(tkt.ticketDate) + `</div>`;
    }
    r+=`<div class="ticket__detail">Assigned To: `+tkt.agent.fullName+`</div>`;
    r+=`<div class="ticket__detail">SLA: `+tkt.sla.name+`</div>`;
    r+=`<div class="ticket__detail">Sbmitted By: `+tkt.user.fullName+`</div>`;
    r+=`<div class="ticket__detail">User Priority: `+tkt.priority+`</div>`;
    r+=`<div class="ticket__detail">Campus: `+tkt.campus.name+` - `+tkt.department.name+`</div>`;
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