export const formatDelay = (delay: number): string => {
  return (delay > 0 ? "+" : "-") + timeSinceString(Math.abs(delay));
};

export const timeSincePretty = (
  seconds: number
): {
  hrs: number;
  min: number;
  sec: number;
  msec: number;
} => {
  let minutes = seconds / 60;
  let msec = minutes * 60 * 1000;
  let hh = Math.floor(msec / 1000 / 60 / 60);
  msec -= hh * 1000 * 60 * 60;
  let mm = Math.floor(msec / 1000 / 60);
  msec -= mm * 1000 * 60;
  let ss = Math.floor(msec / 1000);
  msec -= ss * 1000;
  return {
    hrs: hh,
    min: mm,
    sec: ss,
    msec: msec
  };
};

export const timeSinceString = (minutes: number): string => {
  let diff = timeSincePretty(minutes);
  let formatted = "";
  if (diff.hrs !== 0) formatted += diff.hrs + "h";
  if (diff.min !== 0) formatted += diff.min + "m";
  if (formatted.length < 1) formatted = "0m";
  return formatted;
};
