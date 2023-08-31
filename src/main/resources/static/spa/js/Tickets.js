import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Tickets");
    }

    async getHtml() {
        let tickets = await getRemoteTickets()

        let r = '<h1>Tickets</h1>';
        for (let i in tickets) {
            let obj = tickets[i];
            let arayDate = obj.ticketDate.split("T");
            let fmtDate = arayDate[0];
            let arrayTime = arayDate[1].split(":");
            let fmtTime = arrayTime[0]+":"+arrayTime[1];
            let fmtDateTime = fmtDate+" "+fmtTime;

            r = r + `<div class="list__item" data-nav-ticket="`+obj.id+`">`;

            r = r + `<div class="list__Item-line">`;
            r = r + `<div class="item-id">`+obj.id+`</div>`;
            r = r + `<div class="item-user">`+obj.user.fullName+`</div>`;
            r = r + `<div class="item-date">`+fmtDateTime+`</div>`;
            r = r + `</div>`;

            r = r + `<div class="list__Item-line">`;
            r = r + `<div class="item-detail">`+obj.ticketDetail+`</div>`;
            r = r + `</div>`;

            r = r + `</div>`;
        }

        return r;
    }
}

async function getRemoteTickets() {
    const response = await fetch('/api/v1/app/tickets');
    const tickets = await response.json();
    console.log(tickets);
    return tickets;
}