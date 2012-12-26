<!doctype html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Livereload Documentation</title>
    <style>
      #page-wrap {
      width: 800px;
      margin: 0 auto;
      }

      body {
      font: 15px/1.4 sans-serif;
      }
      code {
      background: #fefefe;
      padding: 10px 5px;
      display: block;
      }

      #footer {
      margin-top: 20px;
      text-align: right;
      font-size: 11px;
      color: #888;
      }

      #footer .doc {
      float: left;
      }

      #footer a {
      color: #555;
      }
    </style>
  </head>
  <body>

    <div id="page-wrap">
      <h1>Livereload, free your hand from F5</h1>
      <h3>Directory been watched</h3>
      <p>{{ root }}</p>
      <h3>HTTP File Server</h3>

      <p>Point you browser: <a href="http://{{ server-host }}/">http://{{ server-host }}</a></p>

      <h3>Live Reload</h3>
      Add
      <div>
        <code>
          &lt;script src="http://{{ server-host }}/d/js"&gt;&lt;/script&gt;
        </code>
      </div>
      to the bottom of your html, and start live reload

      <p>Have fun.</p>

      <div id="footer">
        <p>
          <a href="https://github.com/shenfeng/livereload">Livereload</a>,
          built by <a href="http://shenfeng.me">Feng Shen</a>,
          with <a href="http://clojure.org">Clojure</a>,
          <a href="https://github.com/shenfeng/http-kit">Http-kit</a>.
          <a href="/d/doc">Documentation</a>
        </p>
      </div>
    </div>
  </body>
</html>
