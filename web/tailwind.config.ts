import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#081224",
        slate: "#1f2b46",
        gold: "#ffb642"
      }
    }
  },
  plugins: []
} satisfies Config;
