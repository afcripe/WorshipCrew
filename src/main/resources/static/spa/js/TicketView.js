import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Ticket");
    }

    async getHtml() {
        let ticket = await getRemoteTicket(this.params.id, this.params.token);
        let notes = await getRemoteTicketNotes(this.params.id, this.params.token);
        let returnHTML = htmlTicket(ticket);
        for (let n in notes) {
            let noteObj = notes[n];
            returnHTML += htmlTicketNotes(noteObj);
        }
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteTicket(id, token) {
    const response = await fetch('/api/v1/app/ticket/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteTicketNotes(id, token) {
    const response = await fetch('/api/v1/app/notelist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteTicketAgents(id, token) {
    const response = await fetch('/api/v1/app/agentlist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

function toggleDetail() {
    let detail = document.getElementById("groupDetailExpand");
    let btnExpand = document.getElementById("btnDetailExpand");
    let btnCollapse = document.getElementById("btnDetailCollapse");
    if ( detail.classList.contains("ticket__collapse-group") ) {
        detail.classList.remove("ticket__hide-group");
        detail.classList.remove("ticket__collapse-group");
        btnExpand.style.display = "none";
        btnCollapse.style.display = "block";

    } else {
        detail.classList.add("ticket__collapse-group");
        btnExpand.style.display = "block";
        btnCollapse.style.display = "none";
        detail.classList.add("ticket__hide-group");
    }

}

function htmlTicket(tkt) {
    let r = `<div class="ticket__group">`;
    r+=`<div class="ticket__left ticket__title">Ticket: `+tkt.id+`</div>`;
    r+=`<div class="ticket__right ticket__title">`+tkt.ticketStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="ticket__detail">`+tkt.ticketDetail+`</div>`;

    r+=`<div class="ticket__detail-group">`;
    r+=`<i id="btnDetailExpand" class="bi bi-arrows-expand ticket__detail-expand" data-ticket-detail-toggle></i>`;
    r+=`<i id="btnDetailCollapse" class="bi bi-arrows-collapse ticket__detail-expand" style="display: none" data-ticket-detail-toggle></i>`;
    r+=`<div class="ticket__detail">Date Due: `+formatDate(tkt.ticketDue)+`</div>`;
    r+=`<div id="groupDetailExpand" class="ticket__expand-group ticket__collapse-group ticket__hide-group">`;
    r+=`<div class="ticket__detail">Date Sbmitted: `+formatDate(tkt.ticketDate)+`</div>`;
    if (tkt.closeDate) {
        r += `<div class="ticket__detail">` + formatDate(tkt.ticketDate) + `</div>`;
    }
    r+=`<div class="ticket__detail">Assigned To: `+tkt.agent.fullName+`</div>`;
    r+=`<div class="ticket__detail">SLA: `+tkt.sla.name+`</div>`;
    r+=`<div class="ticket__detail">Sbmitted By: `+tkt.user.fullName+`</div>`;
    r+=`<div class="ticket__detail">User Priority: `+tkt.priority+`</div>`;
    r+=`<div class="ticket__detail">Campus: `+tkt.campus.name+` - `+tkt.department.name+`</div>`;
    r+=`</div></div>`;

    return r;
}

function htmlTicketNotes(note) {
    let r=`<div class="ticket__note">`;
    if (note.agentNote) {
        r+=`<div class="ticket__note-title ticket__note-title-agent">`;
        r+=`<div class="ticket__note-user">`+note.user+`</div>`;
        r+=`<div class="ticket__note-date">`+formatDate(note.noteDate)+`</div>`;
        if (note.notePrivate) {
            r+=`<i class="bi bi-eye-slash ticket__note-private"></i>`;
        } else {
            r+=`<i class="bi bi-eye ticket__note-private"></i>`;
        }
        r+=`</div>`;
    } else {
        r+=`<div class="ticket__note-title">`;
        r+=`<div class="ticket__note-user">`+note.user+`</div>`;
        r+=`<div class="ticket__note-date">`+formatDate(note.noteDate)+`</div>`;
        if (note.notePrivate) {
            r+=`<i class="bi bi-eye-slash ticket__note-private"></i>`;
        } else {
            r+=`<i class="bi bi-eye ticket__note-private"></i>`;
        }
        r+=`</div>`;
    }

    r+=`<div class="ticket__note-detail">`+note.detail+`</div>`;
    r+=`<div class="ticket__detail">`;
    for (let i in note.images) {
        r+=`<div class="ticket__note-images">`;
        r+=`<img src="`+note.images[i].fileLocation+`" alt="`+note.images[i].name+`" class="selectable-image" data-nav-image="`+note.images[i].fileLocation+`">`;
        r+=`</div>`;
    }
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

export { toggleDetail };