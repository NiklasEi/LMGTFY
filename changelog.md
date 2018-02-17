v 1.4.0:
- make the actual link invisible in chat
- links are not send as player message anymore, but clearly marked as plugin message with click and hover event
- possible cool down for sending links in chat
  - bypass permission: lmgtfy.bypass
- reload command can now be used to change the lmgtfy mode

v 1.3.0:
- fix possible problems with copying default language files, if jar is renamed to include chars that need encoding in an URL (like a space in the typical situation "LMGTFY (2).jar") 
- boolean option renamed to: useShorteningService
- custom shortening services

v 1.2.1:
- add mandarin default lang file
- more/updated comments in classes and config/language files

v 1.2.0:
- add 5 new search engines
- support 4 different engines with lmgtfy
- add bStats

v 1.1.1:
- fix mix up of no_perm and no_query messages on loading
- remove unnecessary spaces from default messages