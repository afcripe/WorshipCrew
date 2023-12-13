import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Requests");
    }

    async getHtml() {
        this.setAppProgress(20);
        let user = await getRemoteUser(this.params.id, this.params.token);
        let requestsOpen = await getRemoteRequestsByUserOpen(this.params.id, this.params.token);
        let requestsClosed = await getRemoteRequestsByUserClosed(this.params.id, this.params.token);
        let returnHTML = `<div class="list__group">
                        <div class="list__group-item-grow"><h2>Requests by `+user.fullName+`</h2></div></div>`;

        this.setAppProgress(60);
        returnHTML += `<div id="requestList">`;

        if (requestsOpen.length > 0) {
            returnHTML += `<h3>Open Requests</h3>`;
            for (let m in requestsOpen) {
                let mr = requestsOpen[m];
                returnHTML += htmlRequestLine(mr);
            }
        }

        if (requestsClosed.length > 0) {
            returnHTML += `<h3>Closed Requests</h3>`;
            for (let r in requestsClosed) {
                let cr = requestsClosed[r];
                returnHTML += htmlRequestLine(cr);
            }
        }
        if (requestsOpen.length === 0 && requestsClosed.length === 0) {
            returnHTML += '<h4>No Requests Found</h4>';
        }
        returnHTML += `</div>`;

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

async function getRemoteRequestsByUserOpen(id, token) {
    const response = await fetch('/api/v1/app/request/listbyuser/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const requests = await response.json();
    const status = response.status;
    return requests;
}

async function getRemoteRequestsByUserClosed(id, token) {
    const response = await fetch('/api/v1/app/request/listbyuser/'+id+'?status=closed', {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const requests = await response.json();
    const status = response.status;
    return requests;
}

function htmlRequestLine(req) {
    let r = "";
    r+=`<div class="list__item" data-link-request="`+req.id+`">`;
    r+=`<div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="appList__item-id" data-link-request="`+req.id+`">`+req.user+`</div>`;
    r+=`<div class="appList__item-right" data-link-request="`+req.id+`">`+formatDate(req.date)+`</div>`;
    r+=`</div><div class="list__Item-line" data-link-request="`+req.id+`">`;
    r+=`<div class="appList__item-detail" data-link-request="`+req.id+`">`+req.detail+`</div>`;
    r+=`<div class="appList__item-right appList__item-outline" data-link-request="`+req.id+`" style="white-space: nowrap">Items: `+req.itemCount+`</div>`;
    r+=`</div></div>`;

    return r;
}

function formatDate(dte) {
    let strDate = dte.split("T")[0];
    let strTime = dte.split("T")[1];
    let partsDate = strDate.split("-");
    let partTime = strTime.split(":");
    return partsDate[1] + " / " + partsDate[2] + " / " + partsDate[0];
}
