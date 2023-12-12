import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Request");
    }

    async getHtml() {
        this.setAppProgress(20);
        let req = await getRemoteRequest(this.params.id, this.params.token);
        let items = await getRemoteRequestItems(this.params.id, this.params.token);
        let agents = await getRemoteSupervisors(this.params.id, this.params.token);
        let returnHTML = htmlRequest(req);

        this.setAppProgress(40);
        for (let i in items) {
            let itemObj = items[i];
            returnHTML += htmlRequestItems(itemObj);
        }

        this.setAppProgress(60);
        returnHTML += htmlRequestAgents(agents, req);

        this.setAppProgress(80);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }

    async getNotification() {
        let canAcknowledge = await getRemoteAcknowledge(this.params.id, this.params.token);

        if(canAcknowledge) {
            // ToDo - Popup Acknowledge Request
            let r = `<div>`;
            r += `<form><div class="form-content form__popup-content">`;

            r += `<h4>Acknowledge Request</h4>`;

            r += `<div class="request__item-detail">`;
            r += `This Request is still in 'Submitted' status. Would you like to acknowledge this request as 'Received'?`;
            r += `<input type="hidden" id="requestAcknowledgedId" value="` + this.params.id + `">`;
            r += `</div>`;

            r += `<div class="request__item-detail detail-padding-bottom">`;
            r += `<div class="request__item-field-center">`;
            r += `<button type="button" class="btn btn-sm btn-store" data-form-request-acknowledge="accept">Acknowledge</button>`;
            r += `</div>`;
            r += `<div class="request__item-field-center">`;
            r += `<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-acknowledge="reject">Close</button>`;
            r += `</div>`;
            r += `</div>`;

            r += `</div></form></div>`;

            let dialogHTML = document.createElement("div");
            dialogHTML.id = "formRequest";
            dialogHTML.classList.add("form__popup");
            dialogHTML.innerHTML = r;
            document.body.appendChild(dialogHTML);
        }
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

async function getRemoteAcknowledge(id, token) {
    let formData = new FormData();
        formData.set("id", id);

    const response = await fetch('/api/v1/app/request/getacknowledge', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    const status = response.status;
    if (status === 200) {
        return true;
    } else {
        return false;
    }
}

async function getRemoteRequest(id, token) {
    const response = await fetch('/api/v1/app/request/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteRequestItems(id, token) {
    const response = await fetch('/api/v1/app/request/itemlist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteRequestItem(id, token) {
    const response = await fetch('/api/v1/app/request/item/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteSupervisors(id, token) {
    const response = await fetch('/api/v1/app/request/supervisorlist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteRequestHistory(id, token) {
    const response = await fetch('/api/v1/app/request/history/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function showRequestHistory(reqID, token) {
    let notes = await getRemoteRequestHistory(reqID, token);
    let returnHTML = htmlHistory(notes);
    returnHTML = returnHTML.replaceAll("\n","");

    let dialog=document.createElement("dialog");
    dialog.id="historyViewer";
    dialog.classList.add("history-viewer__dialog");

    dialog.innerHTML = returnHTML;

    document.getElementById("app").appendChild(dialog);
    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("historyViewer").remove();
    });

    dialog.showModal();
}

async function updateRequest(reqID, token) {
    let options = await getOrderStatusOptions(token);
    let req = await getRemoteRequest(reqID, token);
    let returnHTML = htmlDialogUpdateRequest(req, options);
    let dialogHTML =  document.createElement("div");
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
}

async function updateRequestItem(itemID, token) {
    let options = await getOrderStatusOptions(token);
    let item = await getItemOrderStatus(itemID, token);
    let returnHTML = htmlDialogUpdateRequestItem(item, options);
    let dialogHTML =  document.createElement("div");
    dialogHTML.id = "formRequestItem";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
}

async function updateRequestAgent(reqID, token) {
    let users = await getOrderSupervisorOptions(token);
    let req = await getRemoteRequest(reqID, token);
    let returnHTML = htmlDialogRequestAgent(users, req);
    let dialogHTML =  document.createElement("div");
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
}

async function updateRequestItemAgent(itemID, token) {
    let users = await getOrderSupervisorOptions(token);
    let item = await getRemoteRequestItem(itemID, token);
    let returnHTML = htmlDialogRequestItemAgent(users, item);
    let dialogHTML =  document.createElement("div");
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
}

async function updateSupervisor(reqID, token) {
    let users = await getOrderSupervisorOptions(token);
    let req = await getRemoteRequest(reqID, token);
    let returnHTML = htmlDialogUpdateSupervisors(users, req);
    let dialogHTML =  document.createElement("div");
    dialogHTML.id = "formRequest";
    dialogHTML.classList.add("form__popup");
    dialogHTML.innerHTML = returnHTML;
    document.body.appendChild(dialogHTML);
}

async function getOrderStatusOptions(token) {
    const response = await fetch('/api/v1/app/request/orderstatusoptions', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();

}

async function getItemOrderStatus(itemId, token) {
    const response = await fetch('/api/v1/app/request/itemorderstatus/'+itemId, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getOrderSupervisorOptions(token) {
    const response = await fetch('/api/v1/app/request/supervisoroptions', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();

}

async function postRequestAcknowledged(token) {
    let formData = new FormData();
    formData.set("id", document.getElementById("requestAcknowledgedId").value);
    const response = await fetch('/api/v1/app/request/setacknowledge', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    let rsp =  await response.json();
    return document.getElementById("requestAcknowledgedId").value;
}

async function postRequestStatus(token) {
    let formData = new FormData();
    formData.set("requestId", document.getElementById("requestStatusId").value);
    formData.set("requestStatus", document.getElementById("statusOrderSelect").value);
    formData.set("requestNote", document.getElementById("statusOrderNote").value);
    formData.set("items", document.getElementById('statusItems').checked);

    const response = await fetch('/api/v1/app/request/changerequeststatus', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function postRequestItemStatus(token) {
    let formData = new FormData();
    formData.set("requestId", document.getElementById("requestItemStatusId").value);
    formData.set("requestStatus", document.getElementById("statusItemSelect").value);
    formData.set("requestNote", document.getElementById("statusItemNote").value);

    const response = await fetch('/api/v1/app/request/changeitemstatus', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function postRequestAddAgent(token) {
    let formData = new FormData();
    formData.set("requestId", document.getElementById("requestAgentId").value);
    formData.set("userId", document.getElementById('requestAgentSelect').value);
    formData.set("primary", "true");
    formData.set("items", document.getElementById('assignItems').checked);

    const response = await fetch('/api/v1/app/request/addsupervisor', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function postRequestItemAddAgent(token) {
    let formData = new FormData();
    formData.set("requestId", document.getElementById("requestItemAgentId").value);
    formData.set("userId", document.getElementById('requestItemAgentSelect').value);
    formData.set("primary", "true");

    const response = await fetch('/api/v1/app/request/changeitemsupervisor', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function postRequestAddSupervisor(token) {
    let formData = new FormData();
    formData.set("requestId", document.getElementById("requestSuperId").value);
    formData.set("userId", document.getElementById('supervisorSelect').value);
    formData.set("primary", "false");

    const response = await fetch('/api/v1/app/request/addsupervisor', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function postRequestRemoveSupervisor(requestId, userId, token) {
    let formData = new FormData();
    formData.set("requestId", requestId);
    formData.set("userId", userId);
    formData.set("primary", "false");

    const response = await fetch('/api/v1/app/request/removesupervisor', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    let rsp = await response.json();
    const status = response.status;
    if (status === 200) {
        let aId = "agent-"+userId;
        document.getElementById(aId).remove();
    } else {
        console.log("cannot remove required Supervisor")
    }
}

function htmlRequest(req) {
    let r = `<div class="request__group">`;
    r+=`<div class="ticket__left ticket__title">Request: `+req.id+`</div>`;
    r+=`<div class="ticket__right ticket__title">`+req.orderStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="ticket__detail">Reason: `+req.requestNote+`</div>`;

    r+=`<div class="ticket__detail-group">`;
    if ( req.editable ) {
        r+=`<div class="request__item-top-right">`;
        r+=`<button class="btn btn-sm btn-store btn-space" data-request-status="` + req.id + `"><i class="bi bi-info-lg" data-request-status="` + req.id + `"></i></button>`;
        r+=`<button class="btn btn-sm btn-store btn-space" data-request-agent="` + req.id + `"><i class="bi bi-person-fill-add" data-request-agent="` + req.id + `"></i></button>`;
        r+=`</div>`;
    }
    r+=`<div class="ticket__detail">Date: `+formatDate(req.requestDate)+`</div>`;
    r+=`<div id="groupDetailExpand" class="ticket__expand-group">`;
    r+=`<div class="ticket__detail">Submitted By: `+req.user.fullName+`</div>`;
    r+=`<div class="ticket__detail">Supervisor: `+req.supervisor.fullName+`</div>`;
    r+=`<div class="ticket__detail">Total Items: `+req.itemCount+`</div>`;
    r+=`<div class="request__item-bottom-right"><button class="btn btn-sm btn-generic" data-request-history="` + req.id + `">History</button></div>`;

    r+=`</div></div>`;

    r+=`<div class="request__item-detail detail-padding-top">`;
    r+=`<div class="request__item-field-header">Requested Items</div>`;
    r+=`<div class="request__item-field-grow"><hr class="item__hr"></div>`;
    r+=`</div>`;

    return r;
}

function htmlRequestItems(item) {
    let r=`<div class="request__item">`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-images">`;
    r+=`<img src="`+item.image.fileLocation+`" alt="`+item.image.name+`" class="selectable-image" data-nav-image="`+item.image.fileLocation+`">`;
    r+=`</div>`;
    r+=`<div class="request__item-field-left">`+item.productName+`</div>`;
    r+=`<div class="request__item-field-right">Items: `+item.count+`</div>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-field-left">Option: `+item.details+`</div>`;
    r+=`<div class="request__item-field-right">`+item.itemStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-field-left">`+item.supervisor.fullName+`</div>`;
    if ( item.editable ) {
        r+=`<div class="request__item-field-right">`;
        r+=`<button class="btn btn-sm btn-store btn-space" data-request-item-status="` + item.id + `"><i class="bi bi-info-lg" data-request-item-status="` + item.id + `"></i></button>`;
        r+=`<button class="btn btn-sm btn-store btn-space" data-request-item-agent="` + item.id + `"><i class="bi bi-person-fill-add" data-request-item-agent="` + item.id + `"></i></button>`;
        r+=`</div>`;
    }
    r+=`</div>`;

    r+=`</div>`;
    return r;
}

function htmlRequestAgents(agents, req) {
    let r=`<div class="request__item-detail detail-padding-top">`;
    r+=`<div class="request__item-field-header">Supervisors</div>`;
    r+=`<div class="request__item-field-grow"><hr></div>`;
    r+=`<div class="request__item-field-right">`;
    if (req.editable) {
        r+=`<button class="btn btn-sm btn-store" data-request-supervisor="`+req.id+`"> + </button>`;
    }
    r+=`</div></div>`;

    r+=`<div class="request__item">`;
    if ( agents.length > 0) {
        for (let a in agents) {
            let agent = agents[a];
            r += `<div class="request__item-detail" id="agent-`+agent.id+`">`;
            r += `<div class="request__item-field-left">` + agent.fullName + `</div>`;
            r += `<div class="request__item-field-right">`;
            if (req.editable) {
                r+=`<button class="btn btn-sm btn-outline-store" data-request-id="`+req.id+`" 
                        data-request-supervisor-remove="`+agent.id+`"> x </button>`;
            }
            r += `</div></div>`;
        }
    }
    r+=`</div>`;
    return r;
}

function htmlHistory(notes) {
    let r=`<div class="history-viewer__div"><div class="history-viewer__content">`;
    if ( notes.length > 0) {
        for (let n in notes) {
            let note = notes[n];
            r+=`<div class="request__item">`;

            r+=`<div class="request__item-detail">`;
            r+=`<div class="request__item-field-left">` + formatDate(note.noteDate) + `</div>`;
            r+=`<div class="request__item-field-right">` + note.user.fullName + `</div>`;
            r+=`</div>`;

            r+=`<div class="request__item-detail">`;
            r+=`<div class="request__item-field-left">Request Status: ` + note.orderStatus+ `</div>`;
            r+=`</div>`;

            r+=`<div class="request__item-detail">`;
            r+=`<div class="request__item-field-left">Note: ` + note.orderNote + `</div>`;
            r+=`</div>`;

            r+=`</div>`;
        }
    }

    r+=`</div>`;

    r+=`<div class="history-viewer__close">`;
    r+=`<div class="request__item-field-grow"></div>`;
    r+=`<div class="request__item-field-right"><button id="btnViewerClose" class="btn btn-sm btn-outline-store">close</button>&nbsp;</div>`;
    r+=`</div></div>`;

    return r;
}

function htmlDialogUpdateRequest(req, options) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Update Request Status</h4>`;
    r+=`<input type="hidden" id="requestStatusId" value="`+req.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="statusOrderSelect" class="form-control">`;
    for (let o in options) {
        if (req.orderStatus === options[o]) {
            r+=`<option value="`+options[o]+`" selected>`+options[o]+`</option>`;
        }else {
            r+=`<option value="`+options[o]+`">`+options[o]+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="form-control-label">Set for All Items</div>`;
    r+=`<input id="statusItems" type="checkbox" class="form-check">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<textArea id="statusOrderNote" class="form-control""></textArea>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request-status="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-status="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogUpdateRequestItem(item, options) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Update Item Status</h4>`;
    r+=`<input type="hidden" id="requestItemStatusRequestId" value="`+item.requestId+`">`;
    r+=`<input type="hidden" id="requestItemStatusId" value="`+item.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="statusItemSelect" class="form-control">`;
    for (let o in options) {
        if (item.itemStatus === options[o]) {
            r+=`<option value="`+options[o]+`" selected>`+options[o]+`</option>`;
        }else {
            r+=`<option value="`+options[o]+`">`+options[o]+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<textArea id="statusItemNote" class="form-control""></textArea>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request-item-status="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-item-status="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogRequestAgent(users, req) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Re-assign Request</h4>`;
    r+=`<input type="hidden" id="requestAgentId" value="`+req.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="requestAgentSelect" class="form-control">`;
    for (let u in users) {
        let user = users[u];
        r+=`<option value="`+user.id+`">`+user.fullName+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="form-control-label">Re-assign All Items</div>`;
    r+=`<input id="assignItems" type="checkbox" class="form-check">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request-agent="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-agent="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogRequestItemAgent(users, item) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Re-assign Request Item</h4>`;
    r+=`<input type="hidden" id="requestAgentAddId" value="`+item.requestId+`">`;
    r+=`<input type="hidden" id="requestItemAgentId" value="`+item.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="requestItemAgentSelect" class="form-control">`;
    for (let u in users) {
        let user = users[u];
        r+=`<option value="`+user.id+`">`+user.fullName+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request-item-agent="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-item-agent="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogUpdateSupervisors(users, req) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Add Supervisor</h4>`;
    r+=`<input type="hidden" id="requestSuperId" value="`+req.id+`">`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<select id="supervisorSelect" class="form-control">`;
    for (let u in users) {
        let user = users[u];
        r+=`<option value="`+user.id+`">`+user.fullName+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request-supervisor="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-supervisor="cancel">Cancel</button>`;
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
    return partsDate[1] + " / " + partsDate[2] + " / " + partsDate[0] + partTime[0] + ":" + partTime[1];
}

export { updateRequest, updateRequestItem, showRequestHistory, updateSupervisor, updateRequestAgent, updateRequestItemAgent, postRequestAcknowledged,
    postRequestStatus, postRequestItemStatus, postRequestAddAgent, postRequestItemAddAgent, postRequestAddSupervisor, postRequestRemoveSupervisor};