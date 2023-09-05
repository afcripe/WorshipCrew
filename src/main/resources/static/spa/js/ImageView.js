function imageDialog(src) {
    let dialog=document.createElement("dialog");
        dialog.id="imageViewer";
        dialog.addEventListener("click", (event) => {
            event.preventDefault();
            document.getElementById("imageViewer").remove();
        });

    let image=document.createElement("img");
        image.src=src;
        dialog.appendChild(image);

    document.getElementById("app").appendChild(dialog);
    dialog.showModal();
}

export { imageDialog };