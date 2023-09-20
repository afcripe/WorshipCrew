import AbstractView from "./AbstractView.js";

export default class DashboardClass extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("New Ticket");
    }

    async getHtml() {
        this.setAppProgress(20);

        let returnHTML = htmlForm();

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

function htmlForm() {
    let r=`<form><div class="form-content">`;

    r+=`<div class="ticket__group">`;
    r+=`<div class="ticket__left ticket__title">New Ticket</div>`;
    r+=`<div class="ticket__right ticket__title"></div>`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Summary</label>`;
    r+=`<input type="text" id="ticketSummary" class="form-control">`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Detail</label>`;
    r+=`<textarea id="ticketDetail" class="form-control" style="height: 3rem;"></textarea>`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Priority</label>`;
    r+=`<select id="ticketPriority" class="form-control">`;
    r+=`<option value="0">Select Priority</option>`;
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>SLA</label>`;
    r+=`<select id="ticketSLA" class="form-control">`;
    r+=`<option value="0">Select SLA</option>`;
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Campus</label>`;
    r+=`<select id="ticketCampus" class="form-control">`;
    r+=`<option value="0">Select Campus</option>`;
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-col detail-padding-bottom">`;
    r+=`<label>Department</label>`;
    r+=`<select id="ticketDepartment" class="form-control">`;
    r+=`<option value="0">Select Department</option>`;
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-row detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn btn-support" data-form-ticket-new="update">Submite</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn btn-outline-cancel" data-nav-link="tickets">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}