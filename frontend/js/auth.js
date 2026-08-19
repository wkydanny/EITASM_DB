const currentUser =
    sessionStorage.getItem("eitasmCurrentUser") ||
    localStorage.getItem("eitasmCurrentUser");

if (!currentUser) {
    window.location.replace("login.html");
}