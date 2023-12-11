import AbstractView from "./AbstractView.js";

export default class DashboardClass extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("New Ticket");
    }

    async getHtml() {
        this.setAppProgress(20);
        let priorities = await getRemoteTicketPriority(this.params.token);
        let campuses = await getRemoteTicketCampus(this.params.token);
        let departments = await getRemoteTicketDepartment(this.params.token);

        let returnHTML = htmlForm(priorities, campuses, departments);

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

async function getRemoteTicketPriority(token) {
    const response = await fetch('/api/v1/app/ticket/getprioritylist', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const priorities = await response.json();
    const status = response.status;
    return priorities;
}

async function getRemoteTicketCampus(token) {
    const response = await fetch('/api/v1/app/ticket/getcampuslist', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const campuses = await response.json();
    const status = response.status;
    return campuses;
}

async function getRemoteTicketDepartment(token) {
    const response = await fetch('/api/v1/app/ticket/getdepartmentlist', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const departments = await response.json();
    const status = response.status;
    return departments;
}

async function postNewTicket(token) {
    setAppProgress(20);
    const formSummary = document.getElementById("ticketSummary");
    const formDetail = document.getElementById("ticketDetail");
    const formPriority = document.getElementById("ticketPriority");
    const formCampus = document.getElementById("ticketCampus");
    const formDepartment = document.getElementById("ticketDepartment");

    let formData = new FormData();
        formData.set("summary", formSummary.value);
        formData.set("details", formDetail.value);
        formData.set("priority", formPriority.value);
        formData.set("campus", formCampus.value);
        formData.set("department", formDepartment.value);
        formData.set("image", "");

    setAppProgress(40);

    const response = await fetch('/api/v1/app/ticket/newticket', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    let rsp = await response.json();

    setAppProgress(80);
    return rsp.name;
}

function htmlForm(priorities, campuses, departments) {
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
    r+=`<option value="0" selected>Select Priority</option>`;
    for (let p in priorities) {
        let pri = priorities[p]
        r+=`<option value="`+pri.priority+`">`+pri.priority+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Campus</label>`;
    r+=`<select id="ticketCampus" class="form-control">`;
    r+=`<option value="0" selected>Select Campus</option>`;
    for (let c in campuses) {
        let cam = campuses[c]
        r+=`<option value="`+cam.id+`">`+cam.name+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-col detail-padding-bottom">`;
    r+=`<label>Department</label>`;
    r+=`<select id="ticketDepartment" class="form-control">`;
    r+=`<option value="0" selected>Select Department</option>`;
    for (let d in departments) {
        let dep = departments[d]
        r+=`<option value="`+dep.id+`">`+dep.name+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="form-group-row detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn btn-support" data-form-ticket-new="submit">Submit</button>`;
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

export { postNewTicket };