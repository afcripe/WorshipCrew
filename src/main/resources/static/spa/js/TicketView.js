import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Ticket");
    }

    async getHtml() {
        this.setAppProgress(20);
        let ticket = await getRemoteTicket(this.params.id, this.params.token);
        let notes = await getRemoteTicketNotes(this.params.id, this.params.token);
        let returnHTML = htmlTicket(ticket);

        this.setAppProgress(60);
        for (let n in notes) {
            let noteObj = notes[n];
            returnHTML += htmlTicketNotes(noteObj);
        }

        this.setAppProgress(80);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
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

async function showTicketAgents(ticketID, token) {
    let agents = await getRemoteTicketAgents(ticketID, token);
    let returnHTML = htmlAgents(agents, ticketID);
    returnHTML = returnHTML.replaceAll("\n","");

    let dialog=document.createElement("dialog");
    dialog.id="agentViewer";
    dialog.classList.add("history-viewer__dialog");

    dialog.innerHTML = returnHTML;

    document.getElementById("app").appendChild(dialog);
    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("agentViewer").remove();
    });

    dialog.showModal();
}

async function updateAgent(ticketID, token) {
    setAppProgress(20);
    let users = await getAgentOptions(token);
    let returnHTML = htmlDialogAddAgent(users, ticketID);
    let dialogHTML =  document.createElement("div");
    setAppProgress(60);
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
    setAppProgress(101);
}

async function updateNote(ticketID, token) {
    setAppProgress(20);
    let ticket = await getRemoteTicket(ticketID, token);
    let returnHTML = htmlDialogAddNote(ticket);
    let dialogHTML =  document.createElement("div");
    setAppProgress(60);
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    setAppProgress(101);

    document.body.appendChild(dialogHTML);
}

async function updateTicketStatus(ticketID, token) {
    setAppProgress(20);
    let options = await getTicketStatusOptions(token);
    let tkt = await getRemoteTicket(ticketID, token);
    let returnHTML = htmlDialogUpdateStatus(tkt, options);
    let dialogHTML =  document.createElement("div");
    setAppProgress(60);
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
    setAppProgress(101);
}

async function updateTicketSLA(ticketID, token) {
    setAppProgress(20);
    let options = await getTicketSLAOptions(token);
    let tkt = await getRemoteTicket(ticketID, token);
    let returnHTML = htmlDialogUpdateSLA(tkt, options);
    let dialogHTML =  document.createElement("div");
    setAppProgress(60);
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
    setAppProgress(101);
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
    const response = await fetch('/api/v1/app/ticket/notelist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteTicketAgents(id, token) {
    const response = await fetch('/api/v1/app/ticket/agentlist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getAgentOptions(token) {
    const response = await fetch('/api/v1/app/ticket/agentoptions', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();

}

async function getTicketStatusOptions(token) {
    const response = await fetch('/api/v1/app/ticket/ticketstatusoptions', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();

}

async function getTicketSLAOptions(token) {
    const response = await fetch('/api/v1/app/ticket/slaoptions', {
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

async function postTicketNote(token) {
    setAppProgress(20);
    let uploadedImages = ""
    let children = document.getElementById('imagePath').children;
    for (let i = 0; i < children.length; i++) {
        let idArray = children[i].id.split("-");
        let pic = idArray[idArray.length-1];
        uploadedImages = uploadedImages+pic+" ";
    }

    setAppProgress(40);
    let pvt = "false";
    try { pvt = document.getElementById("ticketPrivate").checked } catch (e) {}

    let formData = new FormData();
    formData.set("isPrivate", pvt);
    formData.set("detail", document.getElementById("ticketNote").value);
    formData.set("images", uploadedImages);
    formData.set("ticketId", document.getElementById("ticketNoteId").value);

    setAppProgress(60);
    const response = await fetch('/api/v1/app/ticket/postnote', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });

    setAppProgress(80);
    return await response.json();
}

async function postTicketStatus(token) {
    setAppProgress(20);
    let formData = new FormData();
    formData.set("id", document.getElementById("ticketStatusId").value);
    formData.set("status", document.getElementById("statusTicketSelect").value);
    formData.set("note", "");

    const response = await fetch('/api/v1/app/ticket/poststatus', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    setAppProgress(80);
    return await response.json();
}

async function postTicketSLA(token) {
    setAppProgress(20);
    let formData = new FormData();
    formData.set("id", document.getElementById("slaTicketSelect").value);
    formData.set("name", document.getElementById("ticketSLAId").value);

    const response = await fetch('/api/v1/app/ticket/postsla', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    setAppProgress(80);
    return await response.json();
}

async function postTicketAddAgent(token) {
    setAppProgress(20);
    let formData = new FormData();
    formData.set("id", document.getElementById("ticketAgentId").value);
    formData.set("userId", document.getElementById('agentSelect').value);
    formData.set("primary", document.getElementById('agentPrimary').checked);

    const response = await fetch('/api/v1/app/ticket/addagent', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    setAppProgress(80);
    return await response.json();
}

async function postTicketRemoveAgent(ticketId, userId, token) {
    let formData = new FormData();
    formData.set("id", ticketId);
    formData.set("userId", userId);
    formData.set("primary", "false");

    const response = await fetch('/api/v1/app/ticket/removeagent', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    let rsp = await response.json();

    document.getElementById("agentViewer").remove();
    await showTicketAgents(ticketId, token);
}

// HTML //

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
    if (tkt.sla) {
        r+=`<div class="ticket__detail">SLA: ` + tkt.sla.name + `</div>`;
    } else {
        r+=`<div class="ticket__detail">SLA: </div>`;
    }

    r+=`<div id="groupDetailExpand" class="ticket__expand-group ticket__collapse-group ticket__hide-group">`;
    r+=`<div class="ticket__detail">Date Sbmitted: `+formatDate(tkt.ticketDate)+`</div>`;
    if (tkt.closeDate) {
        r += `<div class="ticket__detail">` + formatDate(tkt.ticketDate) + `</div>`;
    }
    r+=`<div class="ticket__detail">Assigned To: `+tkt.agent.fullName+`</div>`;
    r+=`<div class="ticket__detail">Sbmitted By: `+tkt.user.fullName+`</div>`;
    r+=`<div class="ticket__detail">User Priority: `+tkt.priority+`</div>`;
    r+=`<div class="ticket__detail">Campus: `+tkt.campus.name+` - `+tkt.department.name+`</div>`;
    r+=`</div></div>`;

    r+=`<div class="ticket__group-2">`;
    r+=`<div class="ticket__grow"><button class="btn btn-sm btn-support" data-ticket-note="` + tkt.id + `">New Note</button></div>`;
    if (tkt.ticketAgent) {
        r += `<div class="ticket__grow"><button class="btn btn-sm btn-support" data-ticket-status="` + tkt.id + `">Status</button></div>`;
        r += `<div class="ticket__grow"><button class="btn btn-sm btn-support" data-ticket-sla="` + tkt.id + `">SLA</button></div>`;
        r += `<div class="ticket__grow"><button class="btn btn-sm btn-outline-support" data-ticket-agents="` + tkt.id + `">Agents</button></div>`;
    }
    r+=`</div>`;

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

function htmlDialogUpdateStatus(tkt, options) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Update Ticket Status</h4>`;
    r+=`<input type="hidden" id="ticketStatusId" value="`+tkt.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="statusTicketSelect" class="form-control">`;
    for (let o in options) {
        if (tkt.ticketStatus === options[o]) {
            r+=`<option value="`+options[o]+`" selected>`+options[o]+`</option>`;
        }else {
            r+=`<option value="`+options[o]+`">`+options[o]+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-support" data-form-ticket-status="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-ticket-status="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogUpdateSLA(tkt, options) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Update Ticket SLA</h4>`;
    r+=`<input type="hidden" id="ticketSLAId" value="`+tkt.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="slaTicketSelect" class="form-control">`;
    for (let s in options) {
        let tktSLA = options[s]
        if (tkt.sla.id === tktSLA.id) {
            r+=`<option value="`+tktSLA.id+`" selected>`+tktSLA.name+`</option>`;
        }else {
            r+=`<option value="`+tktSLA.id+`">`+tktSLA.name+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-support" data-form-ticket-sla="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-ticket-sla="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlAgents(agents, ticketID) {
    let r=`<div class="history-viewer__div"><div class="history-viewer__content">`;
    if ( agents.length > 0) {
        for (let a in agents) {
            let agent = agents[a];
            r+=`<div class="request__item">`;

            r+=`<div class="request__item-detail">`;
            r+=`<div class="request__item-field-left">` + agent.fullName + `</div>`;
            r+=`<button class="btn btn-sm btn-outline-support" data-ticket-id="`+ticketID+`" 
                        data-ticket-agent-remove="`+agent.id+`"> x </button>`;
            r+=`</div>`;

            r+=`</div>`;
        }
    }

    r+=`</div></div>`;

    r+=`<div class="history-viewer__close">`;
    r+=`<div class="request__item-field-grow">`;
    r+=`<button id="btnAddAgent" class="btn btn-sm btn-support" data-ticket-agent="`+ticketID+`">Add Agent</button></div>`;
    r+=`<div class="request__item-field-right"><button id="btnViewerClose" class="btn btn-sm btn-outline-support">close</button></div>`;
    r+=`</div></div>`;

    return r;
}

function htmlDialogAddNote(tkt) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Add Note</h4>`;
    r+=`<input type="hidden" id="ticketNoteId" value="`+tkt.id+`">`;
    r+=`</div>`;

    if (tkt.ticketAgent) {
        r+=`<div class="request__item-detail detail-padding-bottom">`;
        r+=`Private Note: <input type="checkbox" id="ticketPrivate">`;
        r+=`</div>`;
    } else {
        r+=`<div class="request__item-detail detail-padding-bottom" style="display: none">`;
        r+=`Private Note: <input type="checkbox" id="ticketPrivate">`;
        r+=`</div>`;
    }

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<textArea id="ticketNote" class="form-control""></textArea>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<input type="hidden" name="image" id="image">`;
    r+=`<input type="file" id="imageFile" style="display: none;" multiple data-ticket-image-upload="`+tkt.id+`">`;
    r+=`<button type="button" class="btn btn-sm btn-support" onclick="document.getElementById('imageFile').click();">Attach Image</button>`;
    r+=`<div id="imagePath"></div>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-support" data-form-ticket-note="update" data-ticket-id="`+tkt.id+`">Add</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-ticket-note="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogAddAgent(users, ticketID) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Add Agent</h4>`;
    r+=`<input type="hidden" id="ticketAgentId" value="`+ticketID+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="agentSelect" class="form-control">`;
    for (let u in users) {
        let user = users[u];
        r+=`<option value="`+user.id+`">`+user.fullName+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`Set As Primary: <input type="checkbox" id="agentPrimary">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-support" data-form-ticket-agent="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-ticket-agent="cancel">Cancel</button>`;
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

export { toggleDetail, showTicketAgents, updateAgent, updateNote, updateTicketSLA, postTicketSLA,
    updateTicketStatus, postTicketNote, postTicketStatus, postTicketAddAgent, postTicketRemoveAgent };