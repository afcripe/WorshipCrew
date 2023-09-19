export default class {
    constructor(params) {
        this.params = params;
    }

    setTitle(title) {
        document.title = title;
    }

    setAppProgress(prg) {
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

    async getHtml() {
        return "";
    }

    async getNotification() {
        return null;
    }
}