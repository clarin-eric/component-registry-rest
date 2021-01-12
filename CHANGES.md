# Bug Fixes and Features

For upgrade instructions, see [UPGRADE.md](UPGRADE.md)

## Release Component Registry 2.4.0 (January 2021)

- Using [CMD toolkit version 1.2.4](https://github.com/clarin-eric/cmdi-toolkit/releases/tag/1.2.4)
- Cues for tools namespace changed to `http://www.clarin.eu/cmd/cues/1` (as per the [CMDI 1.2 specification](https://www.clarin.eu/cmdi1.2-specification))
- Components and profiles have a newly added boolean property `recommended`

## Release Component Registry 2.3.1 (February 2020)
- Various libraries upgraded
- Small changes in administration interface (driven by upgrade to Wicket 8)

## Release Component Registry 2.3.0 (December 2019)
- Java 11 required
- Many libraries updated, including Spring and Wicket
- Front end no longer included in back end build
- Updater utility removed
- Documentation in markdown

## Releasee ComponentRegistry-2.2.2 (April 2017)
- New front end version with fixed behaviour of 'Edit (as new)' button
- Extended administration interface, includes user management and improved component editing

## Releasee ComponentRegistry-2.2.1 (February 2017)
- Fixed issue on authentication when user not previously registered

## Releasee ComponentRegistry-2.2.0 (December 2016)
- Component lifecycle management:
	- Items in the public, private and team spaces can be given the deprecated status.
	- A successor can be assigned to deprecated items.
	- Successors are linked to from deprecated items in the user interface.
	- In the Component Editor, an item can be replaced by its successor with a single click
	- Private items can be optionally be published as draft, leading to a public item with development status that can still be modified
	- A filter in the Component Browser allows for viewing of development, production and/or deprecated items
	- The different statuses are visually distinguished in the Component Browser and Editor
- Component Editor enhancements and additions:
	- A CMDI 1.1/CMDI 1.2 mode selector makes it easy to edit with compatibility in mind
	- Multilingual documentation can be added to components, elements and attribute
	- "Automatic value expressions" can be added to elements and attributes
	- Regular expressions as element/attribute type ('pattern') are now validated
	- Added client side validation
	- Controlled vocabularies:
		- Two vocabulary types are now supported: open and closed vocabularies
		- External vocabularies can be linked to open and closed vocabularies
		- Available CLAVAS vocabularies can be selected from a list without having to enter a URI
		- External vocabularies can be imported into a closed vocabulary
		- A batch editing (CSV) mode for vocabulary items has been added
		- The tabular vocabulary editor supports cell navigation using the tab key
- Component Editor fixes:
	- Missing value schemes (types) of elements and attributes are highlighted in the editor
- UI and usability improvements/fixes:
	- Icons have been added to many buttons making it easier to find and recognise oft needed functionalities
	- Items can be published (as draft or for production) from the component browser
	- Linked components can be accessed via a link in their title in the viewer of the Component Browser
	- After updating an item and/or when the space changes, the Component Browser's table is scrolled to the position of the selected item
	- Concept links for inline components are now shown in the viewer
	- Linked component description shown in the Component Browser's viewer and Component Editor
	- 'Move to team' dropdown now shows full list also after save as new
	- Various performance improvements
- Back end improvements and fixes:
	- Components are expanded during validation, thus catching more potential issues
	- Improved authorisation checks on public/production items
	- Fixed the availability of interactive API documentation (at /api-docs)
	- Indexing of lifecycle status information for fast filtered retrieval
	
Note: for more information about CMDI 1.2 features, see <https://www.clarin.eu/cmdi12>.

### Milestones:
- Back end: <https://github.com/clarin-eric/component-registry-rest/milestone/2>
- Front end: <https://github.com/clarin-eric/component-registry-front-end/milestone/3>

## Releasee ComponentRegistry-2.1.2 (July 2016)
- Fixed logging issues

## Releasee ComponentRegistry-2.1.1 (July 2016)
- Fixed a number of issues related to submitting and publishing specs from the front end

## Releasee ComponentRegistry-2.1.0 (July 2016)
- Switch to CMDI 1.2 (see UPDATE)
- Front end adaptations for CMDI 1.2
	<https://github.com/clarin-eric/component-registry-front-end/issues/29>
	<https://github.com/clarin-eric/component-registry-front-end/issues/30>
	<https://github.com/clarin-eric/component-registry-front-end/issues/32>
	<https://github.com/clarin-eric/component-registry-front-end/issues/33>
	<https://github.com/clarin-eric/component-registry-front-end/issues/34>
- Concept search fixes
	<https://github.com/clarin-eric/component-registry-front-end/issues/21>
	<https://github.com/clarin-eric/component-registry-front-end/issues/36>
	<https://github.com/clarin-eric/component-registry-front-end/issues/38>
- Fixed issues on cancelling edit
	<https://github.com/clarin-eric/component-registry-front-end/issues/23>
	<https://github.com/clarin-eric/component-registry-front-end/issues/39>
- It is no longer possible for 'private' to be selected in components panel in editor when editing a group item
	<https://github.com/clarin-eric/component-registry-front-end/issues/35>
- Improved group management in admin console
	<https://github.com/clarin-eric/component-registry-rest/issues/3>
	<https://github.com/clarin-eric/component-registry-rest/issues/4>

## Releasee ComponentRegistry-2.0.5 (June 2016)
- Added a 'read only' mode. For instructions, see 'INSTALL'. No further changes.

## Releasee ComponentRegistry-2.0.4 (March 2016)
- Fixed an issue with the editing of vocabularies of legacy components <https://github.com/clarin-eric/component-registry-front-end/issues/28>

## Releasee ComponentRegistry-2.0.3 (March 2016)
- Fixed display of profiles with linked root component <https://github.com/clarin-eric/component-registry-front-end/issues/26>
- Added link to front end to "index.jsp" auth debugging page

## Releasee ComponentRegistry-2.0.2 (February 2016)
- Added Piwik tracking code

## Releasee ComponentRegistry-2.0.1 (February 2016)
- Cosmetic fixes

## Releasee ComponentRegistry-2.0 (February 2016)
- New React.JS based front end

## Releasee ComponentRegistry-1.14.6 (12 August 2015)
- Modification in the processing of CCR responses to account for a planned change in the response format

## Releasee ComponentRegistry-1.14.5 (2 February 2015)
- The Component Registry now connects to the CLARIN Concept Registry instead of ISOcat. https://trac.clarin.eu/ticket/731
- Group components can now be saved as new in the private workspace. https://trac.clarin.eu/ticket/717
- Reduced noice in log output. https://trac.clarin.eu/ticket/714 ; https://trac.clarin.eu/ticket/715

## Releasee ComponentRegistry-1.14.2 (2 December 2014)
- Fixed a bug which caused incomplete component specification expansion

## Releasee ComponentRegistry-1.14.1 (1 December 2014)
- Component and profile XML and XSD are available without authentication

## Releasee ComponentRegistry-1.14.0 (28 November 2014)
- Merged profiles and components on persistence level into BaseComponent
- Only one component DAO left: ComponentDao
- DB upgrade script which migrates profiles into components
- Rest service XML using uniform element "componentId" in comments instead of "componentDescriptionId" or "profileDescriptionId"
- User groups have been added, for more information see <https://trac.clarin.eu/wiki/ComponentRegistryAndEditor#Management>. https://trac.clarin.eu/ticket/143
- Fixed erroneous link for child components with direct item links. https://trac.clarin.eu/ticket/322
- Fixed search by component ID for profiles. https://trac.clarin.eu/ticket/225
- Simple data categories no longer show up in ISOcat search. https://trac.clarin.eu/ticket/306
- Improvement in admin interface. https://trac.clarin.eu/ticket/294
- Internal refactoring w.r.t. persistence layer. https://trac.clarin.eu/ticket/360
- Added Swagger documentation for REST API. https://trac.clarin.eu/ticket/692

## Releasee ComponentRegistry-1.13.0 (25 February 2013)
- Added RSS feeds for public profiles, components, and comments. https://trac.clarin.eu/ticket/180
- Added help link to UI. https://trac.clarin.eu/ticket/222
- Fixed superfluous expansion of components and profiles on save. https://trac.clarin.eu/ticket/223
- Better handling of large specifications, fixed hangs and crashes due to lengthy XML. https://trac.clarin.eu/ticket/268
- Profiles and components in the Component Browser can be filtered by id. https://trac.clarin.eu/ticket/225
- Concept link URIs get validated in the Component Editor. https://trac.clarin.eu/ticket/249

## Releasee ComponentRegistry-1.12.0 (21 August 2012):
- REST service produces 404 status when component requested that does not exist.
- Users can delete their own comments from the Flex UI. http://trac.clarin.eu/ticket/185
- Attribute name uniqueness check in editor of Flex UI. http://trac.clarin.eu/ticket/184
- Added a search button to the ISOCat search dialog. http://trac.clarin.eu/ticket/190
- Improved usability of the type edit dialog of the component editor in the Flex UI. http://trac.clarin.eu/ticket/191
- Component browser of Flex UI now has create and edit buttons. Slight re-arrangement of other browser components. http://trac.clarin.eu/ticket/201
- User gets warned when an action in the component editor leads to discarding pending changes. http://trac.clarin.eu/ticket/202
- If string element is set to multilingual, max occurences field gets disabled. http://trac.clarin.eu/ticket/208
- Added 'close' link to user settings page. http://trac.clarin.eu/ticket/156
- Removed 'unbounded' option from minimal occurrences drop-down in editor. http://trac.clarin.eu/ticket/210
- Made elements and components in component editor collapsable. http://trac.clarin.eu/ticket/217
- Removed top-level tabs in the Flex UI, added buttons to browser, editor and import for navigation. http://trac.clarin.eu/ticket/218

## Releasee ComponentRegistry-1.11.1 (10 April 2012):
- Fixed caching of w3c xml.xsd schema in validator
- Fixes issue with user settings page users without user entry in database

## Releasee ComponentRegistry-1.11.0 (3 April 2012):
- Upgraded to Saxon-HE 9.4
- Validation of component specifications now also supports schematron rules in the general component schema.
- Flex component editor does client side checks for duplicate component names.
- Comments can be posted on profiles and components through a set of new REST calls, with support in the Flex UI. http://trac.clarin.eu/ticket/144
- Data type 'dateTime' has been added as ValueScheme for elements. http://trac.clarin.eu/ticket/163
- Recursion detection takes place at every component/profile registration or update. http://trac.clarin.eu/ticket/168
- Users can set their display name through a web form linked from the Flex UI. http://trac.clarin.eu/ticket/156
- 'Save' button in editor is only enabled when editing existing components/profiles from workspace. http://trac.clarin.eu/ticket/165

## Releasee ComponentRegistry-1.10.0 (4 November 2011):
- Attributes can be added and edited on components. http://trac.clarin.eu/ticket/125
- ConceptLinks can be assigned to attributes of components and elements. http://trac.clarin.eu/ticket/125#comment:3
- ConceptLinks are enabled for Components. The search is filtered to show 'container' type concepts only. http://trac.clarin.eu/ticket/141
- XML browser ('component preview') shows the group name if available. http://trac.clarin.eu/ticket/159
- Added the mdEditor parameter to registry/profiles REST call to filter on profiles that should appear in e.g. Arbil. http://trac.clarin.eu/ticket/160
- An 'about' screen can be triggered from the context menu that shows some info including the application version. http://trac.clarin.eu/ticket/106

## Releasee ComponentRegistry-1.9.1 (29 September 2011):
- When recursion is detected while expanding (for XML or XSD), an exception is thrown, which will lead to an error response to the user rather than an infinite loop and no response. http://trac.clarin.eu/ticket/148

## Releasee ComponentRegistry-1.9 (22 September 2011):
- Save private components works even when component is used, warning message is shown in Flex UI http://trac.clarin.eu/ticket/134

## Releasee ComponentRegistry-1.8.1 (29 June 2011):
- Fixed some issues with UTF-8 handling http://trac.clarin.eu/ticket/133

## Releasee ComponentRegistry-1.8 (8 June 2011):
- Backend reimplemented to use relational database instead of filesystem storage
- Specify group for profiles http://trac.clarin.eu/ticket/49
- Header name from specification copied to description when importing http://trac.clarin.eu/ticket/51
- Fixed fold-out bug http://trac.clarin.eu/ticket/99

## Releasee ComponentRegistry-1.7 (26 January 2011):
- Publishing means moving the private component/profile to public space. http://trac.clarin.eu/ticket/57
- Edit pane is reloaded with saved component/profile. http://trac.clarin.eu/ticket/91
- Created "show info" menu item that gives the xsd link and a bookmarkable link to open registry on selected item. http://trac.clarin.eu/ticket/59
- Profiles in private workspace can have a public xsd. http://trac.clarin.eu/ticket/52

## Releasee ComponentRegistry-1.6.1 (19 November 2010):
- fixed bug where registry jumps to the editor screen upon a delete
- added tooltip to MultiLingual
- added some whitespace to up/down icons
- fixed issue where profiles with no "defining" component could not be edited. A "defining" component is now always added. For example see imdi-profile-instance

## Releasee ComponentRegistry-1.6 (12 November 2010):
- No more dependency on jaxb, use default implementation in java6.
- xml:lang in profiles/components, http://trac.clarin.eu/ticket/10
- Add a save and a save as new button, http://trac.clarin.eu/ticket/11
- Move clear changes button and make behaviour more clear, http://trac.clarin.eu/ticket/12 
- Make browsing open for anyone (lazy login), http://trac.clarin.eu/ticket/14
- "Adding ""null"" to ValueScheme (controlled vocabulary) doesn't show the item in the editor", http://trac.clarin.eu/ticket/15
- "Make download xml, expanded", http://trac.clarin.eu/ticket/16
- Allow reorder of elements/components in editor, http://trac.clarin.eu/ticket/17


## Releasee ComponentRegistry-1.5 (6 September 2010):
- cleanup olds libs dir mpg jars can be taken from clarin maven site.
- update jersey version
- Admin page: cannot delete stuff, which is still used. Need to be able to update it at least at admin's risk. 


## Releasee ComponentRegistry-1.4 (20 August 2010):
- Alternatively make the registry open in the browser showing the component/profile (make them bookmarkable). This involves moving the login process to a later stage and having a completely anonymous browsing registry.
- Fix handling of eduPrincipalName/DisplayName make mapping in registry to keep track of which user has which displayName.
  MIGRATE: add principalName to userMapping.xml, Migrate all descriptions to add userId element.
- Import Descriptions is different from profile description, differs from edit.
- put save button also at the bottom.
- Make console showing tomcat log in admin page.
- Make right click 'insert row' in valuescheme edit. So you can add an entry in the middle of an enumeration
- Ping's don't work and are done through anonymous, it should give a better error handling and the ping should fail on anonymous. People need to be triggered to login again. Probable cause is computer sleeps and ping is not done for more then a while.
- Make admin statistics page.
- The description of some components/profiles is very short. The description in the actual cmdi header is longer and should be copied to the description of the description.xml's. Make a migrate plan for that.
- link to isocat should be searchable on metadata only
- Make isocat link clickable in isocat search field.
- Changed tooltip in search isocat.


## Releasee ComponentRegistry-1.3 (28 June 2010):
- Only admin can delete public items after a month in registry.
- sort ignore case
- Add some user logging.
- Indenting XSD with one space.
- Timeout thing. Flex client no keeps session alive by pinging the server.
- Import screen should have bigger description field (like the editor).
- Switched valueScheme item and appInfo columns and presentation. (Ordering will be better then).
- Create Admin Page, where you can browse repo and edit all items. 
- Create public link to private workspace so arbil can query it? as opposed to let people download xsd at the moment? Fix: Use XSD for now.
- Isocat use complex conceptlinks to elements and simple types to valuescheme enumeration.
- isocat simple/complex search in isocat metadata 'profile'
- Stream xsd instead of creating and sending.
- isocat popup place it in middle of screen.
- Make swf file have version, so we have no more caching problems.
- Add message in admin page that you are logged in as admin user. 
- Conceptlink is selectable from the wrong column in valuescheme edit.
- Fixed definition of multiple vocabularies in XSD. (XSLT fix).
- Do not make popups transparant.

