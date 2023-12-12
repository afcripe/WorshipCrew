import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Requests");
    }

    async getHtml() {
        this.setAppProgress(20);
        let requests = await getRemoteRequestsAll(this.params.token);
        let returnHTML = `<div class="list__group">
                        <div class="list__group-item-grow title__module">Requests</div>
                        <div class="list__group-item-right">
                        <button class="btn btn-sm btn-generic" data-nav-my-requests>View Mine</button></div></div>`;

        this.setAppProgress(60);
        returnHTML += `<div class="list__group">
                        <div class="list__group-item-grow"><h3>All Requests</h3></div>
                        <div class="list__group-item-grow">
                        <select class="form-control" id="allRequestsSorting" style="float: right"
                                onchange="document.getElementById('allRequestsSortingBtn').click()">
                            <option value="requestDate,ASC">Request Date | ASC</option>
                            <option value="requestDate,DESC">Request Date | DESC</option>
                        </select>
                        <button type="button" class="btn btn-generic btn-sm" style="display: none"
                         id="allRequestsSortingBtn" data-nav-all-requests-sort></button>
                        </div></div>`;
        returnHTML += `<div id="requestList">`;

        if (requests.length > 0) {
            for (let m in requests) {
                let mr = requests[m];
                returnHTML += htmlRequestLine(mr);
            }
        } else {
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

async function sortAllRequests (srt,ordr,token) {
    const response = await fetch('/api/v1/app/request/allopen?sortCol='+srt+'&sortOrder='+ordr, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    const requests = await response.json();
    const status = response.status;

    document.getElementById('requestList').innerHTML = "";
    let newHTML = "";
    if (requests.length > 0) {
        for (let n in requests) {
            let mt = requests[n];
            newHTML += htmlRequestLine(mt);
        }
    }
    document.getElementById('requestList').innerHTML = newHTML;
}

async function getRemoteRequestsAll(token) {
    const response = await fetch('/api/v1/app/request/allopen', {
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

export { sortAllRequests }