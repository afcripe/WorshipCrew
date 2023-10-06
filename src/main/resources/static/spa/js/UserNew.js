import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let dirList = await getRemoteDirectors(this.params.token);
        this.setAppProgress(40);
        let campusList = await getRemoteCampus(this.params.token);
        this.setAppProgress(60);
        let deptList = await getRemoteDepartment(this.params.token);
        this.setAppProgress(80);
        let posList = await getRemotePosition(this.params.token);

        let returnHTML = htmlUser(campusList, deptList, posList, dirList);

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

async function getRemoteCampus(token) {
    const response = await fetch('/api/v1/app/users/listcampus', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const campusList = await response.json();
    const status = response.status;
    return campusList;
}

async function getRemoteDepartment(token) {
    const response = await fetch('/api/v1/app/users/listepartment', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const deptList = await response.json();
    const status = response.status;
    return deptList;
}

async function getRemotePosition(token) {
    const response = await fetch('/api/v1/app/users/listeposition', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const posList = await response.json();
    const status = response.status;
    return posList;
}

async function getRemoteDirectors(token) {
    const response = await fetch('/api/v1/app/users/listdirectors', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const dirList = await response.json();
    const status = response.status;
    return dirList;
}

async function postNewUser(token) {
    setAppProgress(20);
    let formData = new FormData();
        formData.set("id", "0");
        formData.set("username", document.getElementById('frmUserName').value);
        formData.set("firstName", document.getElementById('frmUserFirstName').value);
        formData.set("lastName", document.getElementById('frmUserLastName').value);
        formData.set("contactPhone", document.getElementById('frmUserPhone').value);
        formData.set("position", document.getElementById('frmUserPosition').value);
        formData.set("department", document.getElementById('frmUserDepartment').value);
        formData.set("campus", document.getElementById('frmUserCampus').value);
        formData.set("directorId", document.getElementById('frmUserDirector').value);

    const response = await fetch('/api/v1/app/users/newuser', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    setAppProgress(80);
    return await response.json();
}

function htmlUser(campusList, deptList, posList, dirList) {
    let r = `<div class="user__group">`;
    r+=`<div class="ticket__left ticket__title">New User</div>`;
    r+=`</div>`;

    r +=`<div class="user-edit__div"><div class="user-edit__content">`;

    r+=`<div class="user__detail">`;
    r+=`E-mail / Username: <input id="frmUserName" type="email" class="form-control">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`First: <input id="frmUserFirstName" type="text" class="form-control">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Last: <input id="frmUserLastName" type="text" class="form-control">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Phone: <input id="frmUserPhone" type="tel" class="form-control">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;

    r+=`Campus: <select id="frmUserCampus" class="form-control">`;
    for (let c in campusList) {
        let campus = campusList[c];
        r+=`<option value="`+campus.name+`">`+campus.name+`</option>`;
    }
    r+=`</select>`;

    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Department: <select id="frmUserDepartment" class="form-control">`;
    for (let d in deptList) {
        let dep = deptList[d];
        r+=`<option value="`+dep.name+`" selected>`+dep.name+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Position: <select id="frmUserPosition" class="form-control">`;
    for (let p in posList) {
        let pos = posList[p];
        r+=`<option value="`+pos.name+`" selected>`+pos.name+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Supervisor: <select id="frmUserDirector" class="form-control">`;
    for (let dr in dirList) {
        let dir = dirList[dr];
        r+=`<option value="`+dir.id+`" selected>`+dir.fullName+`</option>`;
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="user__detail-group">`;
    r+=`<button class="btn btn-sm btn-wiki" data-from-user-new="new">Add User</button>&nbsp;&nbsp;`;
    r+=`<button class="btn btn-sm btn-generic" data-from-user-new="cancel">Cancel</button>`;
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

function formatPhoneNumber(str) {
    let loc = "";
    let exchange = "";
    let area = "";
    let country = "";

    if (!str) {
        return "";
    }

    if (str.length<=4) {
        return str;
    }
    loc = str.substring(str.length,str.length-4);

    if (str.length<=7) {
        return str.substring(str.length - 4, 0) + "-" + loc;
    }
    exchange = str.substring(str.length-4,str.length-7);

    if (str.length<=10) {
        return "(" + str.substring(str.length-7,0) + ") " + exchange + "-" + loc;
    }
    area = "(" + str.substring(str.length-7,str.length-10) + ") ";

    if (str.length>10) {
        country = "+" + str.substring(str.length-10,0) + " ";
    }
    return country + area + exchange + "-" + loc;
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

export { postNewUser };