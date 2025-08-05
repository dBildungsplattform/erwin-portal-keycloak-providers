const http = require("http");

const server = http.createServer((request, response) => {
  const { rawHeaders, httpVersion, method, socket, url } = request;
  const { remoteAddress, remoteFamily } = socket;

  let errorMessage = null;
  let body = [];
  request.on("data", (chunk) => {
    body.push(chunk);
  });
  request.on("end", () => {
    body = Buffer.concat(body);
    body = body.toString();
  });
  request.on("error", (error) => {
    errorMessage = error.message;
  });

  response.on("finish", () => {
    const { method, url } = request;

    console.log(
      JSON.stringify(
        {
          timestamp: Date.now(),
          method,
          url,
          body,
          errorMessage,
        },
        null,
        4
      )
    );
  });

  process(request, response)
});

const process = (request, response) => {
  setTimeout(() => {
    response.end();
  }, 100);
};

server.listen(8888);
