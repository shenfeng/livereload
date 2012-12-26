<!doctype html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Directory Listing: {{dir}}</title>
    <style>
      body {
      font: 15px/1.4 Monospace;
      }

      #page-wrap {
      margin: 0 auto;
      width: 800px;
      }
      table {
      width: 100%;
      }
      caption {
      font-weight: bold;
      font-size: 18px;
      margin: 20px;
      }

      thead {
      font-weight: bold;
      font-size: 16px;
      background: #DFF0D8;
      }

      thead td {
      padding: 3px 5px;
      }

      tbody td {
      padding: 2px 4px;
      }
      tr:nth-child(2n) {
      background: #eee;n
      }

      tr:nth-child(2n) td {
      background: #eee;
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
      <table>
        <caption>Directory List:
          <a href="/">/</a>
          {{#dirs}}<a href="{{href}}">{{ name }}/</a>{{/dirs}}
        </caption>
        <thead>
          <td>File</td>
          <td>Size</td>
          <td>Last Modified</td>
        </thead>
        {{#files}}
          <tr>
            <td><a href="{{href}}">{{name}}</a></td>
            <td>{{ size }}</td>
            <td>{{ mtime }}</td>
          </tr>
        {{/files}}
      </table>
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
