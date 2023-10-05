import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let userList = await getRemoteUserList(this.params.token);
        let permission = await getRemoteEditPermission(this.params.id, this.params.token);

        let returnHTML = `<div class="list__group"><div class="list__group-item-grow title__module">Users</div>`;

        if (permission.name === 'ADMIN_WRITE' || permission.name === 'USER_SUPERVISOR'|| permission.name === 'USER_WRITE') {
            returnHTML += `<div class="list__group-item-right">
                        <i class="bi bi-person-add title__modul-item" data-user-new></i>
                        </div></div>`;
        }
        returnHTML += `</div>`;

        returnHTML += htmlUsers(userList, permission);

        this.setAppProgress(80);
        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML;
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

async function getRemoteUserList(token) {
    const response = await fetch('/api/v1/app/users/', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const userList = await response.json();
    const status = response.status;
    return userList;
}

async function getRemoteEditPermission(id, token) {
    const response = await fetch('/api/v1/app/users/editpermission', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const perm = await response.json();
    const status = response.status;
    return perm;
}

function htmlUsers(userList, permission) {
    let r = "";
        // `<div className="list__group">
        //     <div className="list__group-item-grow title__module">Users</div>
        //     <div className="list__group-item-right">New User</div></div>`;

    for (let u in userList) {
        let user = userList[u];
        r+=`<div class="user-group" data-user-link="`+user.id+`">`;
        r+=`<div class="user-name" data-user-link="`+user.id+`">`+user.fullName+`</div>`;
        r+=`</div>`;
    }

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