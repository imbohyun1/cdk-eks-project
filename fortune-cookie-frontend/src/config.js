// src/env-config.js.dev

const getConfig = () => {

    if (typeof window !== 'undefined' && window._env_) {
        return {
            API_URL: window._env_.REACT_APP_API_URL,
        };
    }

    return {
        API_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
    };
};

const config = getConfig();
console.log('Final config:', config);

export default config;