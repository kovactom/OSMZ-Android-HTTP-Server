# Android HTTP Server
School project for subject OSMZ which provides HTTP server functionality with camera stream support.

## Notes
* After first run, it is required to allow application to use storage and camera
* After first run, HTTP Server service is automatically started
* When open camera via 'Open camera' button and return back to main screen of application, server must be stopped and started again in order to refresh camera stream (this is needed because preview handler changes in this step)

## Features:
*  HTTP server - supports GET request, directory listing, file download
*  Camera stream - provides online access to camera stream via `http://localhost:12345/camera/stream`
*  Camera picture snapshot - provides online access to last picture snapshot via `http://localhost:12345/camera/snapshot`
*  CGI scripts - provides support for run CGI scripts via HTTP requests, example: `http://localhost:12345/cgi-bin/uptime`
*  Maximum of active connections can be restricted
*  Run as android service in background
