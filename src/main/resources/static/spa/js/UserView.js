import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Resources");
    }

    async getHtml() {
        this.setAppProgress(20);
        let user = await getRemoteUser(this.params.id, this.params.token);
        let director = await getRemoteUserDirector(this.params.id, this.params.token);
        let returnHTML = htmlUser(user, director);

        this.setAppProgress(80);
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

function htmlUser(user, director) {
    let r = `<div class="user__group">`;
    r+=`<div class="ticket__left ticket__title">`+user.fullName+`</div>`;
    r+=`</div>`;

    r+=`<div class="user__detail-group">`;
    r+=`<div class="user__detail">Username: <a href="tel:`+user.username+`">`+user.username+`</a></div>`;
    r+=`<div class="user__detail">Phone: <a href="tel:`+user.contactPhone+`">`+formatPhoneNumber(user.contactPhone)+`</a></div>`;
    r+=`<div class="user__detail">Campus: `+user.campus.name+`</div>`;
    r+=`<div class="user__detail">Department: `+user.department.name+`</div>`;
    r+=`<div class="user__detail">Position: `+user.position.name+`</div>`;
    r+=`<div class="user__detail">Supervisor: `+director.fullName+`</div>`;
    r+=`</div>`;

    r+=`<div class="user__detail-group">`;
    r+=`<h4>Permissions</h4>`;
    for (let i in user.userRoles) {
        let role = user.userRoles[i];
        r+=`<div class="user__permission">`+role.name+`</div>`;
    }
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