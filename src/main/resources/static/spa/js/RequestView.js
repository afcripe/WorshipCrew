import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Request");
    }

    async getHtml() {
        let req = await getRemoteRequest(this.params.id, this.params.token);
        let items = await getRemoteRequestItems(this.params.id, this.params.token);
        let agents = await getRemoteSupervisors(this.params.id, this.params.token);
        let returnHTML = htmlRequest(req);

        for (let i in items) {
            let itemObj = items[i];
            returnHTML += htmlRequestItems(itemObj);
        }

        returnHTML += htmlRequestAgents(agents, req);

        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
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

async function updateSupervisor(reqID, token) {
    let users = await getOrderSupervisorOptions(token);
    let returnHTML = htmlDialogUpdateSupervisors(users);
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

function htmlRequest(req) {
    let r = `<div class="request__group">`;
    r+=`<div class="ticket__left ticket__title">Request: `+req.id+`</div>`;
    r+=`<div class="ticket__right ticket__title">`+req.orderStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="ticket__detail">Reason: `+req.requestNote+`</div>`;

    r+=`<div class="ticket__detail-group">`;
    if ( req.editable ) {
        r += `<div class="request__item-top-right"><button class="btn btn-sm btn-store" data-request="` + req.id + `">Update</button></div>`;
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
        r += `<div class="request__item-field-right"><button class="btn btn-sm btn-store" data-request-item="` + item.id + `">Update</button></div>`;
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
            r += `<div class="request__item-detail">`;
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
    r+=`<textArea id="statusOrderNote" class="form-control""></textArea>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail detail-padding-bottom">`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request="cancel">Cancel</button>`;
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
    r+=`<button type="button" class="btn btn-sm btn-store" data-form-request-item="update">Update</button>`;
    r+=`</div>`;
    r+=`<div class="request__item-field-center">`;
    r+=`<button type="button" class="btn btn-sm btn-outline-cancel" data-form-request-item="cancel">Cancel</button>`;
    r+=`</div>`;
    r+=`</div>`;

    r+=`</div></form></div>`;
    return r;
}

function htmlDialogUpdateSupervisors(users) {
    let r=`<div>`;
    r+=`<form><div class="form-content form__popup-content">`;

    r+=`<div class="request__item-detail">`;
    r+=`<h4>Add Supervisor</h4>`;
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
    r+=`Set As Primary: <input type="checkbox" id="supervisorPrimary">`;
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
    return strDate + " " + partTime[0] + ":" + partTime[1];
}

export { updateRequest, updateRequestItem, showRequestHistory, updateSupervisor };