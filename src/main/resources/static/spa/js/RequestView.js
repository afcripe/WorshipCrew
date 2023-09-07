import AbstractView from "./AbstractView.js";

export default class extends AbstractView {
    constructor(params) {
        super(params);
        this.setTitle("Request");
    }

    async getHtml() {
        let req = await getRemoteRequest(this.params.id, this.params.token);
        let items = await getRemoteRequestItems(this.params.id, this.params.token);
        let returnHTML = htmlRequest(req);

        for (let i in items) {
            let itemObj = items[i];
            returnHTML += htmlRequestItems(itemObj);
        }

        returnHTML = returnHTML.replaceAll("\n","");
        return returnHTML.replaceAll("\n","");
    }
}

async function getRemoteRequest(id, token) {
    const response = await fetch('/api/v1/app/request/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

async function getRemoteRequestItems(id, token) {
    const response = await fetch('/api/v1/app/itemlist/'+id, {
        headers: {
            authorization: "Bearer "+token
        }
    });
    return await response.json();
}

function htmlRequest(req) {
    let r = `<div class="request__group">`;
    r+=`<div class="ticket__left ticket__title">Request: `+req.id+`</div>`;
    r+=`<div class="ticket__right ticket__title">`+req.orderStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="ticket__detail">Reason: `+req.requestNote+`</div>`;

    r+=`<div class="ticket__detail-group">`;
    r+=`<div class="request__item-top-right"><button class="btn btn-sm btn-store" disabled>Update</button></div>`;
    r+=`<div class="ticket__detail">Date: `+formatDate(req.requestDate)+`</div>`;
    r+=`<div id="groupDetailExpand" class="ticket__expand-group">`;
    r+=`<div class="ticket__detail">Submitted By: `+req.user.fullName+`</div>`;
    r+=`<div class="ticket__detail">Total Items: `+req.itemCount+`</div>`;
    r+=`</div></div>`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-field-header">Requested Items</div>`;
    r+=`<div class="request__item-field-grow"><hr class="item__hr"></div>`;
    r+=`</div>`;

    return r;
}
function htmlRequestItems(item) {
    let r=`<div class="request__item">`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-images">`;
    r+=`<img src="`+item.image.fileLocation+`" alt="`+item.image.name+`" class="selectable-image" data-nav-image="`+item.image.fileLocation+`">`;
    r+=`</div>`;
    r+=`<div class="request__item-field-left">`+item.productName+`</div>`;
    r+=`<div class="request__item-field-right">Items: `+item.count+`</div>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-field-left">Option: `+item.details+`</div>`;
    r+=`<div class="request__item-field-right">`+item.itemStatus+`</div>`;
    r+=`</div>`;

    r+=`<div class="request__item-detail">`;
    r+=`<div class="request__item-field-left">`+item.supervisor.fullName+`</div>`;
    r+=`<div class="request__item-field-right"><button class="btn btn-sm btn-store" disabled>Update</button></div>`;
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