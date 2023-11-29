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

async function saveRemoteDraftById(id, token) {
    let formData = new FormData();
    formData.set("id", document.getElementById('draftId').value);
    formData.set("subject", document.getElementById('draftSubject').value);
    formData.set("draft", "true");
    formData.set("messageBody", document.getElementById('draftBody').value);
    formData.set("userId", document.getElementById('draftUserId').value);
    formData.set("toUsersId", document.getElementById('drafttoUsersId').value);
    formData.set("toUsersName", document.getElementById('drafttoUsersName').value);

    const response = await fetch('/api/v1/app/messages/save', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    let message =  await response.json();
    document.getElementById('draftId').value = message.id;
    document.getElementById('btnMsgSave').dataset.msgSave = message.id;
    document.getElementById('btnMsgSend').dataset.msgSend = message.id;
    document.getElementById('btnMsgDelete').dataset.msgDelete = message.id;
    document.getElementById('btnMsgDrafts').innerText = "Close";
}

async function sendRemoteDraftById(id, token) {
    let formData = new FormData();
        formData.set("id", document.getElementById('draftId').value);
        formData.set("subject", document.getElementById('draftSubject').value);
        formData.set("draft", "true");
        formData.set("messageBody", document.getElementById('draftBody').value);
        formData.set("userId", document.getElementById('draftUserId').value);
        formData.set("toUsersId", document.getElementById('drafttoUsersId').value);
        formData.set("toUsersName", document.getElementById('drafttoUsersName').value);

    const response = await fetch('/api/v1/app/messages/send', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function deleteRemoteDraftById(id, token) {
    let formData = new FormData();
    formData.set("id", document.getElementById('draftId').value);

    const response = await fetch('/api/v1/app/messages/delete', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
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

    document.getElementById('showCampus').style.display = "none";
    document.getElementById('showDepartment').style.display = "none";
    document.getElementById('showUser').style.display = "none";

    document.getElementById('toGroup').addEventListener("change", (event) => {
        updateGroup(token);
    });
    document.getElementById('toCampus').addEventListener("change", (event) => {
        loadUsers(token);
    });
    document.getElementById('toDepartment').addEventListener("change", (event) => {
        loadUsers(token);
    });

    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("messageToViewer").remove();
    });

    document.getElementById("btnViewerCancel").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("messageToViewer").remove();
    });
    document.getElementById("btnDraftToSelect").addEventListener("click", (event) => {
        event.preventDefault();
        dialogGroupOk();
    });

    await updateGroup(token);

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

async function updateGroup(token) {
    let getCampus;
    let getDepartment;
    let selectedGroup = document.getElementById('toGroup');

    if (selectedGroup !== null) {
        if (selectedGroup.value === "Campus Users"
            || selectedGroup.value === "Department Users"
            || selectedGroup.value === "Campus Department Directors") {
            getCampus = await loadCampuses(token);
            getDepartment = await loadDepartments(token);
        }

        if (selectedGroup.value === "Campus Directors") {
            getCampus =  await loadCampuses(token);
        }

        if (selectedGroup.value === "Regional Department Directors") {
            getDepartment = await loadDepartments(token);
        }
    }
    await loadUsers(token);
}

async function loadCampuses(token) {
    const selectCampus = document.getElementById('toCampus');
    for (let c=0; selectCampus.options.length > 0; c++) {
        selectCampus.options.remove(0);
    }

    const campusResponse = await fetch('/api/v1/app/messages/campuslist', {
        headers: {
            authorization: "Bearer "+token
        }
    }).then(response => {
        return response.json();
    }).then(data => {
        for (let i in data) {
            let cmp = data[i];
            let newOpt = document.createElement('option');
            newOpt.value = cmp;
            newOpt.text = cmp;
            document.getElementById('toCampus').appendChild(newOpt);
        }
        document.getElementById('toCampus')[0].selected = true;
        document.getElementById('showCampus').style.display = "block";
    });
    return document.getElementById('toCampus')[0].value;
}

async function loadDepartments(token) {
    const selectDepartment = document.getElementById('toDepartment');
    for (let d=0; selectDepartment.options.length > 0; d++) {
        selectDepartment.options.remove(0);
    }

    const departmentResponse = await fetch('/api/v1/app/messages/departmentlist', {
        headers: {
            authorization: "Bearer "+token
        }
    }).then(response => {
        return response.json();
    }).then(data => {
        for (let i in data) {
            let dpt = data[i];
            let newOpt = document.createElement('option');
            newOpt.value = dpt;
            newOpt.text = dpt;
            document.getElementById('toDepartment').appendChild(newOpt);
        }
        document.getElementById('toDepartment')[0].selected = true;
        document.getElementById('showDepartment').style.display = "block";
    });
    return document.getElementById('toDepartment')[0].value;
}

async function loadUsers(token) {
    let id = document.getElementById('draftId').value;

    const selectUsers = document.getElementById('toUser');
    for (let u=0; selectUsers.options.length > 0; u++) {
        selectUsers.options.remove(0);
    }

    let formData = new FormData();
    formData.set("campus", document.getElementById('toCampus').value);
    formData.set("department", document.getElementById('toDepartment').value);
    formData.set("listType", document.getElementById('toGroup').value);

    const response = await fetch('/api/v1/app/messages/userlist/'+id, {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    }).then(response => {
        return response.json();
    }).then(data => {
        let noneSelected = true;
        for (let i in data) {
            let user = data[i];
            let newOpt = document.createElement('option');
            newOpt.value = user.id;
            newOpt.text = user.fullName;
            newOpt.selected = user.selected;
            if (user.selected) { noneSelected = false; }
            document.getElementById('toUser').appendChild(newOpt);
        }
        if (noneSelected) { document.getElementById('toUser')[0].selected = true; }
        document.getElementById('showUser').style.display = "block";
    });
}

function dialogGroupOk() {
    const selectUser = document.getElementById('toUser');
    let selectedName = "";
    let selectedValues = "";
    let selectAll = false;
    for (let i=0; i<selectUser.options.length; i++) {
        if (i === 0) {
            if (selectUser.options[i].selected) {
                selectAll = true;
            }
        } else if (selectUser.options[i].selected || selectAll) {
            // set value
            if (selectedValues.length>0) {selectedValues+=", ";}
            selectedValues+=selectUser.options[i].value;
            // set name
            if (selectedName.length>0) {selectedName+=", ";}
            selectedName+=selectUser.options[i].text;
        }
    }
    document.getElementById('drafttoUsersName').value = selectedName;
    document.getElementById('drafttoUsersId').value = selectedValues;
    document.getElementById("messageToViewer").remove();
}

function htmlMessage(msg) {
    let r=`<div class="list__group"><div class="list__group-item-grow">`;
    r+=`<button id="btnMsgSave" class="btn btn-sm btn-wiki" data-msg-save="`+msg.id+`">Save</button>`;
    r+=`&nbsp;`;
    r+=`<button id="btnMsgSend" class="btn btn-sm btn-msg" data-msg-send="`+msg.id+`">Send</button>`;
    r+=`&nbsp;`;
    r+=`<button id="btnMsgDelete" class="btn btn-sm btn-store" data-msg-delete="`+msg.id+`">Delete</button>`;
    r+=`</div><div class="list__group-item-right">
            <button id="btnMsgDrafts" class="btn btn-sm btn-outline-store" data-msg-drafts>`;
            if (msg.id > 0) {
                r+=`Close`;
            } else {
                r+=`Cancel`;
            }
    r+=`</button>
        </div></div>`;

    r+=`<div class="item__hr">&nbsp;</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<button class="btn btn-support btn-generic" data-draft-to="true" style="width: 75px;">To: <i class="bi bi-card-list"></i></button>`;
    r+=`<input type="text" id="drafttoUsersName" class="form-control" value="`+msg.toUsersName+`" disabled>`;
    r+=`<input type="hidden" id="drafttoUsersId" class="form-control" value="`+msg.toUsersId+`" >`;
    r+=`<input type="hidden" id="draftId" class="form-control" value="`+msg.id+`">`;
    r+=`<input type="hidden" id="draftUserId" class="form-control" value="`+msg.userId+`">`;
    r+=`</div>`;

    r+=`<div class="form-group-col">`;
    r+=`<label>Subject</label>`;
    r+=`<input type="text" id="draftSubject" class="form-control" value="`+msg.subject+`">`;
    r+=`</div>`;


    r+=`<div class="item__hr"><hr></div>`;

    r+=`<textarea id="draftBody" style="width: 100%; height: 8rem;">`+msg.messageBody+`</textarea>`;

    return r;
}

function htmlToGroup(groups) {
    let r=`<div class="history-viewer__div"><div class="history-viewer__content">`;

// Groups
    r+=`<div id="showGroup" class="form-group-col">`;
    r+=`<label>Group</label>`;
    r+=`<select id="toGroup" class="form-control">`;
    for (let g in groups) {
        let grp = groups[g]
        r+=`<option value="`+grp+`">`+grp+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

// Campus
    r+=`<div id="showCampus" class="form-group-col">`;
    r+=`<label>Campus</label>`;
    r+=`<select id="toCampus" class="form-control">`;
    r+=`</select>`;
    r+=`</div>`;

// Department
    r+=`<div id="showDepartment" class="form-group-col">`;
    r+=`<label>Department</label>`;
    r+=`<select id="toDepartment" class="form-control">`;
    r+=`</select>`;
    r+=`</div>`;

// Users
    r+=`<div id="showUser" class="form-group-col">`;
    r+=`<label>Users</label>`;
    r+=`<select id="toUser" class="form-control" multiple>`;
    r+=`</select>`;
    r+=`</div>`;

// Buttons
    r+=`<div id="showDepartment" class="form-group-col">`;
    r+=`<div class="request__item-field-right">
            <button id="btnDraftToSelect" class="btn btn-sm btn-msg">Select</button>
            <button id="btnViewerCancel" class="btn btn-sm btn-store">Cancel</button>
        </div>`;
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

export { showRemoteTo, saveRemoteDraftById, sendRemoteDraftById, deleteRemoteDraftById }