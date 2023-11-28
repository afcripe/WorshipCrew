import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Message Draft");
    }

    async getHtml() {
        this.setAppProgress(20);
        let msgDraft = await getRemoteDraftById(this.params.id, this.params.token);
        let returnHTML = htmlMessage(msgDraft);

        this.setAppProgress(50);

        this.setAppProgress(90);
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

async function getRemoteDraftById(id, token) {
    const response = await fetch('/api/v1/app/messages/draft/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}


async function showRemoteTo(token) {
    let groups = await getRemoteGroupList(token);
    let returnHTML = htmlToGroup(groups);
    returnHTML = returnHTML.replaceAll("\n","");

    let dialog=document.createElement("dialog");
    dialog.id="messageToViewer";
    dialog.classList.add("history-viewer__dialog");

    dialog.innerHTML = returnHTML;

    document.getElementById("app").appendChild(dialog);

    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("messageToViewer").remove();
    });

    dialog.showModal();
}

async function getRemoteGroupList(token) {
    const response = await fetch('/api/v1/app/messages/grouplist', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}


function htmlMessage(msg) {
    let r=`<div class="list__group"><div class="list__group-item-grow">`;
    r+=`<button id="tglDrafts" class="btn btn-sm btn-wiki" data-msg-tgl-inbox>Save</button>`;
    r+=`&nbsp;`;
    r+=`<button id="tglDrafts" class="btn btn-sm btn-msg" data-msg-tgl-inbox>Send</button>`;
    r+=`&nbsp;`;
    r+=`<button id="tglDrafts" class="btn btn-sm btn-store" data-msg-tgl-inbox>Delete</button>`;
    r+=`</div><div class="list__group-item-right">
            <button id="tglDrafts" class="btn btn-sm btn-outline-store" data-msg-tgl-inbox>Cancel</button>
        </div></div>`;

    r+=`<div class="item__hr">&nbsp;</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<button class="btn btn-support btn-generic" data-draft-to="true" style="width: 75px;">To: <i class="bi bi-card-list"></i></button>`;
    r+=`<input type="text" id="drafttoUsersName" class="form-control" value="`+msg.toUsersName+`" disabled>`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Subject</label>`;
    r+=`<input type="text" id="draftSubject" class="form-control" value="`+msg.subject+`">`;
    r+=`</div>`;


    r+=`<div class="item__hr"><hr></div>`;

    r+=`<textarea style="width: 100%; height: 8rem;">`+msg.messageBody+`</textarea>`;

    return r;
}

function htmlToGroup(groups) {
    let r=`<div class="history-viewer__div"><div class="history-viewer__content">`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Group</label>`;
    r+=`<select id="toGroup" class="form-control">`;
    r+=`<option value="0" selected>Select Group</option>`;
    for (let g in groups) {
        let grp = groups[g]
        r+=`<option value="`+grp+`">`+grp+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`</div></div>`;

    r+=`<div class="history-viewer__close">`;
    r+=`<div class="message__title">To:</div>`;
    r+=`<div class="request__item-field-right">
            <button id="btnViewerClose" class="btn btn-sm btn-outline-msg">close</button>
        </div>`;
    r+=`</div></div>`;

    return r;
}

function formatDate(dte) {
    if (dte === null) { return "Sending..."; }
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return strDate + " " + partTime[0] + ":" + partTime[1];
}

export { showRemoteTo }