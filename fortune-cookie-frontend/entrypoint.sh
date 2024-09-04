#!/bin/sh

# create 'env-config.js' file
echo "window._env_ = {" > /app/build/env-config.js
echo "  REACT_APP_API_URL: \"$REACT_APP_API_URL\"," >> /app/build/env-config.js
echo "};" >> /app/build/env-config.js

# run serve command
exec serve -s build -l 3000