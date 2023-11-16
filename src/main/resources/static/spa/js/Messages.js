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
        let pDraft = false;
        if (!!this.params.readMessages) {
            pRead = this.params.readMessages === "true";
        }
        if (!!this.params.systemMessages) {
            pSystem = this.params.systemMessages === "true";
        }
        if (!!this.params.draftMessages) {
            pDraft = this.params.draftMessages === "true";
        }

        let messageList = await getRemoteMessages(this.params.token, pRead, pSystem, pDraft);

        let returnHTML = `<div class="list__group">
                                <div class="list__group-item-grow title__module">Messages
                        </div>
                        <div class="list__group-item-right">
                            <i id="tglNewMessage"
                            class="bi bi-envelope-plus color-msg selectableItem message-titleBtn" data-msg-draft="0"></i>
                        </div></div>`;


        returnHTML += `<div class="list__group">
                        <div class="list__group-item-grow">`;
        returnHTML += `<button id="tglInbox" class="btn btn-sm btn-outline-msg" data-msg-tgl-inbox>
                            <i class="bi bi-envelope color-msg" data-msg-tgl-inbox></i>
                        </button>`;
        returnHTML += `&nbsp;`;
        if (pRead && !pDraft) {
            returnHTML += `<button id="tglUnread" class="btn btn-sm btn-msg" data-msg-tgl-read="true">Read</button>`;
        } else {
            returnHTML += `<button id="tglUnread" class="btn btn-sm btn-outline-msg" data-msg-tgl-read="false">Read</button>`;
        }
        returnHTML += `&nbsp;`;
        if (pSystem && !pDraft) {
            returnHTML += `<button id="tglSystem" class="btn btn-sm btn-msg" data-msg-tgl-system="true">System</button>`;
        } else {
            returnHTML += `<button id="tglSystem" class="btn btn-sm btn-outline-msg" data-msg-tgl-system="false">System</button>`;
        }
        returnHTML += `&nbsp;`;
        if (pDraft) {
            returnHTML += `<button id="tglDrafts" class="btn btn-sm btn-wiki" data-msg-tgl-draft="true">Drafts</button>`;
        } else {
            returnHTML += `<button id="tglDrafts" class="btn btn-sm btn-outline-wiki" data-msg-tgl-draft="false">Drafts</button>`;
        }
        returnHTML += `</div></div>`;




        this.setAppProgress(50);
        returnHTML += `<br>`;
        if (messageList.length > 0) {
            for (let n in messageList) {
                let msg = messageList[n];
                if (msg.fromUser === "(Draft)") {
                    returnHTML += htmlDraftLine(msg);
                } else {
                    returnHTML += htmlMessageLine(msg);
                }
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

async function getRemoteMessages(token, read, system, draft) {
    let msgParams = "";
    if (read) {
        msgParams.length>0 ? msgParams+="&" : msgParams+="?";
        msgParams += "read=true";
    }
    if (system) {
        msgParams.length>0 ? msgParams+="&" : msgParams+="?";
        msgParams += "system=true";
    }
    if (draft) {
        msgParams.length>0 ? msgParams+="&" : msgParams+="?";
        msgParams += "draft=true";
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

    document.getElementById("btnViewerRead").addEventListener("click", (event) => {
        event.preventDefault();
        setMessageRead(id, token);
        // document.getElementById("messageViewer").remove();
    });
    document.getElementById("btnViewerUnread").addEventListener("click", (event) => {
        event.preventDefault();
        setMessageUnread(id, token);
        // document.getElementById("messageViewer").remove();
    });

    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("messageViewer").remove();
    });

    let data = setMessageRead(id, token);

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

async function setMessageRead(id, token) {
    document.getElementById("btnViewerRead").style.display = 'none';
    document.getElementById("btnViewerUnread").style.display = 'inline-block';

    const response = await fetch('/api/v1/app/messages/readstate/read/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    let data = await response.json();

    try {
        let divID = "icnRead-"+id;
        document.getElementById(divID).style.display = 'none';
        return data.id;
    } catch (e) {
        console.log("error marking message read")
        return 0;
    }

}

async function setMessageUnread(id, token) {
    document.getElementById("btnViewerUnread").style.display = 'none';
    document.getElementById("btnViewerRead").style.display = 'inline-block';

    const response = await fetch('/api/v1/app/messages/readstate/unread/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    let data = await response.json();

    try {
        let divID = "icnRead-"+id;
        document.getElementById(divID).style.display = 'inline-block';
        return data.id;
    } catch (e) {
        console.log("error marking message read")
        return 0;
    }
}

function htmlMessageLine(msg) {
    let readIcon = "icnRead-"+msg.id;
    let r = "";
    r+=`<div class="list__item" data-msg-message="`+msg.id+`">`;

    r+=`<div class="list__Item-line" data-msg-message="`+msg.id+`">`;

    r+=`<div class="item-id" data-msg-message="`+msg.id+`">From: `+msg.fromUser+`</div>`;

    r+=`<div class="item-user" data-msg-message="`+msg.id+`">&nbsp;</div>`;

    r+=`<div class="item-date" data-msg-message="`+msg.id+`">`+formatDate(msg.dateSent)+`</div>`;

    r+=`</div><div class="list__Item-line" data-msg-message="`+msg.id+`">`;
    r+=`<div class="message__item-detail" data-msg-message="`+msg.id+`">`+msg.subject+`</div>`;
    r+=`<div class="item-date" data-msg-message="`+msg.id+`">
            <i id="`+readIcon+`" class="bi bi-envelope" data-msg-message="`+msg.id+`"`;
    if (msg.read) {
        r+=` style="display: none;"`;
    } else {
        r+=` style="display: inline-block;"`;
    }
    r+=`></i>
        </div>`;

    r+=`</div></div>`;

    return r;
}

function htmlDraftLine(msg) {
    let readIcon = "icnRead-"+msg.id;
    let r = "";
    r+=`<div class="list__item" data-msg-draft="`+msg.id+`">`;

    r+=`<div class="list__Item-line" data-msg-draft="`+msg.id+`">`;

    r+=`<div class="item-id" data-msg-draft="`+msg.id+`">`+msg.fromUser+`</div>`;

    r+=`<div class="item-user" data-msg-draft="`+msg.id+`">&nbsp;</div>`;

    r+=`<div class="item-date" data-msg-draft="`+msg.id+`">Not Sent</div>`;

    r+=`</div><div class="list__Item-line" data-msg-draft="`+msg.id+`">`;
    r+=`<div class="message__item-detail" data-msg-draft="`+msg.id+`">`+msg.subject+`</div>`;
    r+=`<div class="item-date" data-msg-draft="`+msg.id+`">
            <i id="`+readIcon+`" class="bi bi-envelope" style="display: none;" data-msg-draft="`+msg.id+`"></i>
        </div>`;

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
    r+=`<div class="request__item-field-right">
            <button id="btnViewerRead" class="btn btn-sm btn-outline-msg">
               <i class="bi bi-envelope-open selectableItem"></i>
            </button>
            <button id="btnViewerUnread" class="btn btn-sm btn-outline-msg">
                <i class="bi bi-envelope message-button selectableItem"></i>
            </button>
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

export { showRemoteMessage }