(function () {
  if(!window.WebSocket) {return; }
  var TOP = '_r_s_top', conn = new WebSocket("ws://{{server-host}}/d/ws");
  conn.onmessage = function (e) {
    if(window.localStorage) {
      var d = JSON.stringify([window.scrollX, window.scrollY]);
      localStorage.setItem(TOP, d);
    }
    location.reload(true);
  };

  window.onload = function () {
    if(window.localStorage && localStorage.getItem(TOP)) {
      var d = JSON.parse(localStorage.getItem(TOP));
      window.scrollTo(d[0], d[1]);
      localStorage.removeItem(TOP);
    }
  };
  conn.onopen = function (e) {
    console.log("reload connected");
  };
})();
