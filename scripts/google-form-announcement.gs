/*
 * Install in the Google Form's linked Apps Script project as an installable
 * "On form submit" trigger. Store the two values in Script Properties:
 *   MHS_CLUBS_ANNOUNCEMENT_URL = https://api.example.org/integrations/forms/announcements
 *   MHS_CLUBS_FORM_INGEST_SECRET = same value as the server's FORM_INGEST_SECRET
 *
 * Required Form question titles: Club Name, Announcement Title, Announcement Body.
 * Optional title: Any URL Links (if multiple, separate using commas).
 */
function sendAnnouncementToMhsClubs(event) {
  const values = event.namedValues;
  const first = (name) => (values[name] && values[name][0] || '').trim();
  const clubName = first('Club Name');
  const title = first('Announcement Title');
  const messageBody = first('Announcement Body');
  if (!clubName || !title || !messageBody) {
    throw new Error('Club Name, Announcement Title, and Announcement Body are required.');
  }

  const props = PropertiesService.getScriptProperties();
  const url = props.getProperty('MHS_CLUBS_ANNOUNCEMENT_URL');
  const secret = props.getProperty('MHS_CLUBS_FORM_INGEST_SECRET');
  if (!url || !secret) throw new Error('Configure the MHS Clubs URL and ingest secret in Script Properties.');

  const payload = {
    club_name: clubName,
    title: title,
    message_body: messageBody,
    links: first('Any URL Links (if multiple, separate using commas)')
  };
  const response = UrlFetchApp.fetch(url, {
    method: 'post',
    contentType: 'application/json',
    headers: { Authorization: 'Bearer ' + secret },
    payload: JSON.stringify(payload),
    muteHttpExceptions: true
  });
  if (response.getResponseCode() < 200 || response.getResponseCode() >= 300) {
    throw new Error('MHS Clubs rejected the announcement: ' + response.getContentText());
  }
}
