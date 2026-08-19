const mobileMenuButton = document.getElementById("mobileMenuButton");
const sidebar = document.querySelector(".sidebar");

if (mobileMenuButton && sidebar) {

    mobileMenuButton.addEventListener("click", function () {
        sidebar.classList.toggle("sidebar-open");
    });

    document.addEventListener("click", function (event) {

        if (
            sidebar.classList.contains("sidebar-open") &&
            !sidebar.contains(event.target) &&
            !mobileMenuButton.contains(event.target)
        ) {
            sidebar.classList.remove("sidebar-open");
        }

    });

    window.addEventListener("resize", function () {

        if (window.innerWidth > 992) {
            sidebar.classList.remove("sidebar-open");
        }

    });

}