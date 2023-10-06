import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let user = await getRemoteUser(this.params.id, this.params.token);
        this.setAppProgress(40);
        let director = await getRemoteUserDirector(this.params.id, this.params.token);
        this.setAppProgress(60);
        let permission = await getRemoteEditPermission(this.params.id, this.params.token);
        let appUser = await getRemoteGetAppUser(this.params.token);
        this.setAppProgress(80);
        let returnHTML = htmlUser(user, director, permission, appUser);

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

async function getRemoteUser(id, token) {
    const response = await fetch('/api/v1/app/users/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const user = await response.json();
    const status = response.status;
    return user;
}

async function getRemoteUserDirector(id, token) {
    const response = await fetch('/api/v1/app/users/directorof/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const director = await response.json();
    const status = response.status;
    return director;
}

async function getRemoteEditPermission(id, token) {
    const response = await fetch('/api/v1/app/users/editpermission/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const perm = await response.json();
    const status = response.status;
    return perm;
}

async function getRemoteGetAppUser(token) {
    const response = await fetch('/api/v1/app/users/appuser', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const appUser = await response.json();
    const status = response.status;
    return appUser;
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

async function getRemoteDirectors(id, token) {
    const response = await fetch('/api/v1/app/users/listdirectors/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const dirList = await response.json();
    const status = response.status;
    return dirList;
}

async function showUserEdit(id, token) {
    let user = await getRemoteUser(id, token);
    let director = await getRemoteUserDirector(id, token);
    let dirList = await getRemoteDirectors(id, token);
    let campusList = await getRemoteCampus(token);
    let deptList = await getRemoteDepartment(token);
    let posList = await getRemotePosition(token);
    let returnHTML = htmlEditor(user, director, campusList, deptList, posList, dirList);
    returnHTML = returnHTML.replaceAll("\n","");

    let dialog=document.createElement("dialog");
    dialog.id="userEditViewer";
    dialog.classList.add("history-viewer__dialog");

    dialog.innerHTML = returnHTML;

    document.getElementById("app").appendChild(dialog);
    document.getElementById("btnViewerClose").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("userEditViewer").remove();
    });

    dialog.showModal();
}

async function postUpdateAuth(id, auth, token) {
    let formData = new FormData();
        formData.set("id", id);
        formData.set("name", auth);

    const response = await fetch('/api/v1/app/users/updateauth', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    return await response.json();
}

async function postUpdateUser(id, token) {
    setAppProgress(20);
    let formData = new FormData();
        formData.set("id", id);
        formData.set("firstName", document.getElementById('frmUserFirstName').value);
        formData.set("lastName", document.getElementById('frmUserLastName').value);
        formData.set("contactPhone", document.getElementById('frmUserPhone').value);
        formData.set("position", document.getElementById('frmUserPosition').value);
        formData.set("department", document.getElementById('frmUserDepartment').value);
        formData.set("campus", document.getElementById('frmUserCampus').value);
        formData.set("directorId", document.getElementById('frmUserDirector').value);

    const response = await fetch('/api/v1/app/users/updateuser', {
        method: 'POST',
        headers: {
            authorization: "Bearer "+token
        },
        body: formData
    });
    setAppProgress(80);
    return await response.json();
    document.getElementById("userEditViewer").remove();
}

function htmlUser(user, director, permission, appUser) {
    let r = `<div class="user__group">`;
    r+=`<div class="ticket__left ticket__title">`+user.fullName+`</div>`;
    r+=`<button class="btn btn-sm btn-generic"  data-user-edit data-user-update-id="`+user.id+`">Edit</button>`;
    r+=`</div>`;

    r+=`<div class="user__detail-group">`;
    r+=`<div class="user__detail">Username: <a href="tel:`+user.username+`">`+user.username+`</a></div>`;
    r+=`<div class="user__detail">Phone: <a href="tel:`+user.contactPhone+`">`+formatPhoneNumber(user.contactPhone)+`</a></div>`;
    r+=`<div class="user__detail">Campus: `+user.campus.name+`</div>`;
    r+=`<div class="user__detail">Department: `+user.department.name+`</div>`;
    r+=`<div class="user__detail">Position: `+user.position.name+`</div>`;
    r+=`<div class="user__detail">Supervisor: `+director.fullName+`</div>`;
    r+=`</div>`;

    if (permission.name === 'ADMIN_WRITE' || permission.name === 'USER_SUPERVISOR'|| permission.name === 'USER_WRITE') {
        r += `<div class="user__detail-group">`;
        r += `<h4>Permissions</h4>`;

        let rr = false;
        let rw = false;
        let sr = false;
        let sw = false;
        let tw = false;

        let en_rr = false;
        let en_rw = false;
        let en_sr = false;
        let en_sw = false;
        let en_tw = false;

        // set checked permissions
        for (let i in user.userRoles) {
            let role = user.userRoles[i];
            if (role.name === 'RESOURCE_READ') {rr = true;}
            if (role.name === 'RESOURCE_WRITE') {rw = true;}
            if (role.name === 'STORE_READ') {sr = true;}
            if (role.name === 'STORE_WRITE') {sw = true;}
            if (role.name === 'SUPPORT_WRITE') {tw = true;}
        }

        // set enabled permissions
        for (let n in appUser.userRoles) {
            let appRole = appUser.userRoles[n];
            if (appRole.name === 'RESOURCE_READ' ||appRole.name === 'ADMIN_WRITE' ||appRole.name === 'USER_SUPERVISOR') {en_rr = true;}
            if (appRole.name === 'RESOURCE_WRITE' ||appRole.name === 'ADMIN_WRITE' ||appRole.name === 'USER_SUPERVISOR') {en_rw = true;}
            if (appRole.name === 'STORE_READ' ||appRole.name === 'ADMIN_WRITE' ||appRole.name === 'USER_SUPERVISOR') {en_sr = true;}
            if (appRole.name === 'STORE_WRITE' ||appRole.name === 'ADMIN_WRITE' ||appRole.name === 'USER_SUPERVISOR') {en_sw = true;}
            if (appRole.name === 'SUPPORT_WRITE' ||appRole.name === 'ADMIN_WRITE' ||appRole.name === 'USER_SUPERVISOR') {en_tw = true;}
        }

        r += `<div class="user__permission">
                <input type="checkbox" id="inputAuthResourceRead" data-user-update-auth="RESOURCE_READ" data-user-update-id="`+user.id+`"`;
                if (rr) {r += ` checked`;}
                if (!en_rr) {r += ` disabled`;}
            r += `> View Resources</div>`;

        r += `<div class="user__permission">
                <input type="checkbox" id="inputAuthResourceWrite" data-user-update-auth="RESOURCE_WRITE" data-user-update-id="`+user.id+`"`;
                if (rw) {r += ` checked`;}
                if (!en_rw) {r += ` disabled`;}
            r += `> Create Resources</div>`;

        r += `<div class="user__permission">
                <input type="checkbox" id="inputAuthStoreRead" data-user-update-auth="STORE_READ" data-user-update-id="`+user.id+`"`;
                if (sr) {r += ` checked`;}
                if (!en_sr) {r += ` disabled`;}
            r += `> View The Store and Submit Requests</div>`;

        r += `<div class="user__permission">
                <input type="checkbox" id="inputAuthStoreWrite" data-user-update-auth="STORE_WRITE" data-user-update-id="`+user.id+`"`;
                if (sw) {r += ` checked`;}
                if (!en_sw) {r += ` disabled`;}
            r += `> Add and Edit Store Items</div>`;


        r += `<div class="user__permission">
                <input type="checkbox" id="inputAuthSupportWrite" data-user-update-auth="SUPPORT_WRITE" data-user-update-id="`+user.id+`"`;
                if (tw) {r += ` checked`;}
                if (!en_tw) {r += ` disabled`;}
            r += `> Submit Support Tickets</div>`;

        r += `</div>`;
    }

    if (permission.name === 'ADMIN_WRITE') {
        r += `<div class="user__detail-group">`;
        r += `<h4>User Roles</h4>`;
        for (let i in user.userRoles) {
            let role = user.userRoles[i];
            r += `<div class="user__permission">` + role.name + `</div>`;
        }
        r += `</div>`;
    }

    return r;
}

function htmlEditor(user, director, campusList, deptList, posList, dirList) {
    let r=`<div class="user-edit__div"><div class="user-edit__content">`;

    r+=`<div class="user__detail">`;
    r+=`First: <input id="frmUserFirstName" type="text" class="form-control" value="`+user.firstName+`">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Last: <input id="frmUserLastName" type="text" class="form-control" value="`+user.lastName+`">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Phone: <input id="frmUserPhone" type="tel" class="form-control" value="`+user.contactPhone+`">`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;

    r+=`Campus: <select id="frmUserCampus" class="form-control">`;
    for (let c in campusList) {
        let campus = campusList[c];
        if (user.campus.name === campus.name) {
            r+=`<option value="`+campus.name+`" selected>`+campus.name+`</option>`;
        }else {
            r+=`<option value="`+campus.name+`">`+campus.name+`</option>`;
        }
    }
    r+=`</select>`;

    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Department: <select id="frmUserDepartment" class="form-control">`;
    for (let d in deptList) {
        let dep = deptList[d];
        if (user.department.name === dep.name) {
            r+=`<option value="`+dep.name+`" selected>`+dep.name+`</option>`;
        }else {
            r+=`<option value="`+dep.name+`">`+dep.name+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Position: <select id="frmUserPosition" class="form-control">`;
    for (let p in posList) {
        let pos = posList[p];
        if (user.position.name === pos.name) {
            r+=`<option value="`+pos.name+`" selected>`+pos.name+`</option>`;
        }else {
            r+=`<option value="`+pos.name+`">`+pos.name+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="user__detail">`;
    r+=`Supervisor: <select id="frmUserDirector" class="form-control">`;
    for (let dr in dirList) {
        let dir = dirList[dr];
        if (director.fullName === dir.fullName) {
            r+=`<option value="`+dir.id+`" selected>`+dir.fullName+`</option>`;
        }else {
            r+=`<option value="`+dir.id+`">`+dir.fullName+`</option>`;
        }
    }
    r+=`</select>`;
    r+=`</div>`;

    r+=`<div class="user__detail-group">`;
    r+=`<button class="btn btn-sm btn-wiki" data-from-user-edit="`+user.id+`">Update</button>&nbsp;&nbsp;`;
    r+=`<button id="btnViewerClose" class="btn btn-sm btn-generic">Cancel</button>`;
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

export { postUpdateAuth, showUserEdit, postUpdateUser };