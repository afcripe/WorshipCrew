import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        let tickets = await getRemoteTickets();
        let returnHTML = `<h1>Tickets</h1>`;
        for (let i in tickets) {
            let ticket = tickets[i];
            returnHTML += htmlTicketLine(ticket);
        }
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteTickets() {
    const response = await fetch('/api/v1/app/tickets');
    const tickets = await response.json();
    return tickets;
}

async function getRemoteTicketsHTML() {
    const response = await fetch('/api/v1/app/html/listtickets')
        .then(response => {
            return response.text();
        })
        .then(html => {
            return html;
            // Initialize the DOM parser
            // let parser = new DOMParser();
            // Parse the text
            // return parser.parseFromString(html, "text/html");
        });
    let newHTML = await response;
    return newHTML;
}

function htmlTicketLine(tkt) {
    let r = "";
    r+=`<div class="list__item" data-link-ticket="`+tkt.id+`">`;
    r+=`<div class="list__Item-line" data-link-ticket="`+tkt.id+`">`;
    r+=`<div class="item-id" data-link-ticket="`+tkt.id+`">`+tkt.id+`</div>`;
    r+=`<div class="item-user" data-link-ticket="`+tkt.id+`">`+tkt.user.fullName+`</div>`;
    r+=`<div class="item-date" data-link-ticket="`+tkt.id+`">`+formatDate(tkt.ticketDate)+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-ticket="`+tkt.id+`">`;
    r+=`<div class="item-detail" data-link-ticket="`+tkt.id+`">`+tkt.ticketDetail+`</div>`;
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