self.addEventListener('push', function(e) {
    console.log(e);
    let options = {
        body: "This is a new notification",
        icon: "/img/favicon.png"
    };
    e.waitUntil(
        self.registration.showNotification("New Notification", options)
    );
});