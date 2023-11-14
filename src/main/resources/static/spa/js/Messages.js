import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Messages");
    }

    async getHtml() {
        this.setAppProgress(20);

        let pRead = false;
        let pSystem = false;
        if (!!this.params.readMessages) {
            pRead = this.params.readMessages === "true";
        }
        if (!!this.params.systemMessages) {
            pSystem = this.params.systemMessages === "true";
        }

        let messageList = await getRemoteMessages(this.params.token, pRead, pSystem);
        let returnHTML = `<div class="list__group">
                        <div class="list__group-item-grow title__module">Messages</div>
                        <div class="list__group-item-right">`;
        if (pRead) {
            returnHTML += `<button id="tglUnread" class="btn btn-sm btn-msg" data-msg-tgl-read="true">Read</button>`;
        } else {
            returnHTML += `<button id="tglUnread" class="btn btn-sm btn-outline-msg" data-msg-tgl-read="false">Read</button>`;
        }
        returnHTML += `&nbsp;`;
        if (pSystem) {
            returnHTML += `<button id="tglSystem" class="btn btn-sm btn-msg" data-msg-tgl-system="true">System</button>`;
        } else {
            returnHTML += `<button id="tglSystem" class="btn btn-sm btn-outline-msg" data-msg-tgl-system="false">System</button>`;
        }
        returnHTML += `</div></div>`;

        this.setAppProgress(50);
        returnHTML += `<br>`;
        if (messageList.length > 0) {
            for (let n in messageList) {
                let msg = messageList[n];
                returnHTML += htmlMessageLine(msg);
            }
        }

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

async function getRemoteMessages(token, read, system) {
    let msgParams = "";
    if (read) {
        msgParams.length>0 ? msgParams+="&" : msgParams+="?";
        msgParams += "read=true";
    }
    if (system) {
        msgParams.length>0 ? msgParams+="&" : msgParams+="?";
        msgParams += "system=true";
    }

    const response = await fetch('/api/v1/app/messages/listnew'+msgParams, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const messages = await response.json();
    const status = response.status;
    return messages;
}

async function showRemoteMessage(id, token) {
    let msg = await getRemoteMessagesById(id, token);
    let msgBody = await getRemoteMessagesBody(id);
    let returnHTML = htmlMessage(msg, msgBody);
    returnHTML = returnHTML.replaceAll("\n","");

    let dialog=document.createElement("dialog");
    dialog.id="messageViewer";
    dialog.classList.add("history-viewer__dialog");

    dialog.innerHTML = returnHTML;

    document.getElementById("app").appendChild(dialog);
    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("messageViewer").remove();
    });

    dialog.showModal();
}

async function getRemoteMessagesById(id, token) {
    const response = await fetch('/api/v1/app/messages/message/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteMessagesBody(id) {
    const response = await fetch('/app/mailer/content/'+id);
    return await response.text();
}

function htmlMessageLine(msg) {
    let r = "";
    r+=`<div class="list__item" data-msg-message="`+msg.id+`">`;

    r+=`<div class="list__Item-line" data-msg-message="`+msg.id+`">`;

    r+=`<div class="item-id" data-msg-message="`+msg.id+`">From: `+msg.fromUser+`</div>`;

    r+=`<div class="item-user" data-msg-message="`+msg.id+`">&nbsp;</div>`;

    r+=`<div class="item-date" data-msg-message="`+msg.id+`">`+formatDate(msg.dateSent)+`</div>`;

    r+=`</div><div class="list__Item-line" data-msg-message="`+msg.id+`">`;
    r+=`<div class="item-detail" data-msg-message="`+msg.id+`">`+msg.subject+`</div>`;

    r+=`</div></div>`;

    return r;
}

function htmlMessage(msg, msgBody) {
    let r=`<div class="history-viewer__div"><div class="history-viewer__content">`;

    r+=`<div class="list__item" data-msg-message="`+msg.id+`">`;
    r+=`<div class="list__Item-line">`;
    r+=`<div class="item-label item-33">From:</div>`;
    r+=`<div class="item-label ">`+msg.fromUser+`</div>`;
    r+=`</div>`;

    r+=`<div class="list__Item-line">`;
    r+=`<div class="item-label item-33">Date:</div>`;
    r+=`<div class="item-label ">`+formatDate(msg.dateSent)+`</div>`;
    r+=`</div>`;

    r+=`<div class="list__Item-line">`;
    r+=`<div class="item-label item-33">Subject:</div>`;
    r+=`<div class="item-label ">`+msg.subject+`</div>`;
    r+=`</div></div>`;

    r+=`<div class="item__hr"><hr></div>`;

    r+=`<div>`+msgBody+`</div>`;

    r+=`</div></div>`;

    r+=`<div class="history-viewer__close">`;
    r+=`<div class="message__title">`+msg.subject+`</div>`;
    r+=`<div class="request__item-field-right"><button id="btnViewerClose" class="btn btn-sm btn-outline-support">close</button></div>`;
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

export { showRemoteMessage }