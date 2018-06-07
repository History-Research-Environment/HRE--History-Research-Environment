# HRE HISTORY RESEARCH ENVIRONMENT
Towards a history of almost anything

## 1.1 HRE PROJECT AIMS - GENERAL

**SUMMARY - APPLICATION GOALS:**

1.  Build a modular, extendable application which manages the recording,
    analysis and reporting of research in any discipline associated with the
    history of any type of object

2.  Build an application complying with the GPL open source environment

3.  Build an application that will run on Windows, Macintosh and Linux operating
    systems with a single code base

4.  Use Java as the main implementation language

5.  Allow all text to be stored and manipulated as UTF-8 Unicode

6.  Enable the application to be extensible by the use of plug-ins that comply
    with the architecture and use the required registration and application
    interfaces

7.  The use of suitable GPL components is to be favored over the implementation
    of home-grown modules

8.  Application modules should be decoupled and testable within a suitable test
    framework

9.  The application will be able to be used in 2 modes:

    1.  All components in a standalone installation, or

    2.  A user interface running on a client computer with services being
        provided by an external server, not necessarily being cloud-based. NOTE:
        in this configuration there may be concurrent users of the database.

**PROJECT BACKGROUND**

The recording and analysis of historical records is a diverse subject, with the
most common example being *family history*. However, there are two major issues
with current family history applications and their position within the
historical research landscape.

Firstly, they are constrained by their focus on the parenting of children and
birth, death and marriage events. This family-centric focus imposes constraints
on the types of data that can be recorded for later analysis and the types of
inter-entity relationships that can be recorded. For instance, it is very
difficult to present descriptions of the *social family*, with adoption,
children by other partners, etc, and to represent a household versus
a *biological family*. Many anthropological studies require a far wider
definition of 'family'.

Secondly, most current family history applications are intentionally designed to
focus on recording the history of individuals and their genetic relationships to
other individuals. Such applications are not designed, for example, to record
the history of an organisation as it went through structural re-organisation,
takeovers, mergers, splits, CEO replacements, and so on. Some family history
applications may allow creative customizations which can approximate recording
such history, but this is unusual. Since their market is typically aimed at the
casual family genealogist, these limitations are an inherent consequence of
their goal to make the product simple and easy to use by that market for that
one specific purpose. Such design limitations usually exclude more sophisticated
features and tools desired by the more limited market of serious or professional
family history researchers.

However, a more generic and sophisticated application could not only serve
serious family history researchers, but could also extend its market to serious
and professional researchers in many other fields of historical research.

Examples of such fields of research include the history of:

-   organisations, companies, political parties, associations, military units,
    trade unions, etc

-   medical conditions of communities, extended families, etc

-   conflicts (at any level), such as wars, social change, etc

-   anthropological groups (management of paternal/maternal kin-term
    distinctions, multiple naming streams, associated ‘significant others’, etc)

-   land, buildings, vessels, vehicles, etc (for owners, builders, architects,
    tenants, etc)

-   animal lineage, as in the breeding of dogs, cats, horses, whatever

-   botanical lineage, as in plant breeding

-   geographical regions, towns, villages, precincts, etc

-   investigations and research (as in police work, experimental science, etc)

-   art works - sculpture, music, film, radio, TV, drama, paintings, books (for
    artists, scripts, writers, players, producers, editors, restorers, etc)

-   legal cases and the characters/roles in them or affected by them

-   sporting competitions and events

All of these examples have similar underlying data storage and generic
inter-relationship requirements, with the common elements being:

-   a *historical timeline* of *events*

-   recording of *evidence* about these events

-   recording *relationships of entities* to those events and to other entities

-   analysis of the *evidence* to reach *conclusions*

-   preparation of *reports*, which may be research-field dependent, like the
    specialised relationship diagrams used in anthropology

-   *documentation* through the inclusion of documents, images, recordings, etc.

Thus the goal of the HRE project is the creation of a sophisticated application
capable of supporting serious general historical research, rather than a simple
application limited solely to the requirements of recording basic family
history.

## 1.2 HRE AIMS - DATABASE

**CHOICE OF DATABASE:**

There are 3 classes of database use that need to be recognized: embedded single
user, embeddable but multi-concurrent users and standalone TCP/IP networked. It
is recognised that a single user database like SQLite would need too much extra
code as a wrapper to allow for multi-concurrent users.

Although there are at least 3 other options, H2
(<http://www.h2database.com/html/main.html>) seems to be the most efficient
available option.

**DATABASE GOALS:**

1.  The SQL database engine must be GPL (current choice being H2)

2.  There must be a database abstraction interface to allow replacement of the
    database engine if required in the future

3.  There will be a limit on the number of concurrent users of a single database
    (say 10)

4.  There will a limit on the number of records in any database table (say 10M)

5.  Every record of every user data table shall have a persistent record ID.

## 1.3 HRE AIMS – USER INTERFACE

**USER INTERFACE:**

1.  The presentation layer (user GUI and Report Generation) shall be able to
    operate in any supported natural language

2.  Language selection may be changed during application execution

3.  The user interface must be configurable to provide:

    1.  Choice of content and layout with persistence of settings

    2.  Choice of accessibility for vision, color and motor-skills available

    3.  Choice of language

    4.  Efficient pathways for common user workflows

    5.  Balance of the use of screen real estate to achieve clarity of data
        presented

    6.  Standard idioms of look and feel as used in browsers and in complex word
        processing and diagram drawing packages.

4.  All operations that may take a long time to complete must give status
    progress back to the user.

## 1.4 HRE AIMS – DATA MANAGEMENT

**DATA MANAGEMENT:**

1.  User files must be operating system independent in content

2.  The application must have facilities to import digital records in known
    discipline-related formats

3.  The application must have facilities to export digital records to known
    discipline-related formats

4.  All user data must have the capability to store alternative forms in any
    supported language

5.  The user must have the ability to “undo” at least the last data modifying
    command

6.  On a per session-basis, the user must be able identify what data has been
    changed, from what to what and by whom since the start of the session

7.  Selection and ordering of the focus of data manipulation:

    1.  Manual selection and automated selection by use of filters must be
        provided

    2.  Custom filters must be able to be saved and re-used.

8.  The report generator will:

    1.  Provide default standard reports

    2.  Permit users to create their own report templates

    3.  Permit output to a number of word processing and other formats

    4.  Permit creation of documents using larger page sizes (up to A0).

9.  All data management operations that may take a long time to complete must
    provide status progress back to the user.

## 1.5 HRE AIMS - INTEROPERABILITY

**INTEROPERABILITY:**

1.  To be able to export any data within the HRE database to XML format

2.  To provide functionality within the initial implementation that allows for
    the creation of plugin code to extend the functionality of HRE

3.  To provide plug-ins for genealogists to:

    1.  Import project files created by The Master Genealogist v8.05 (and later)
        without loss of information and loss of an ability to use that data as
        it was in TMG

    2.  Import GEDCOM v5.5 standard format files

    3.  Export data from HRE that conforms to the GEDCOM v5.5 standard

    4.  Import data from other genealogical programs by use of the plug-in
        concept.

## 1.6 HRE IMPLEMENTATION STAGES

As this part is a complex document in its own right, refer to ‘1.6
Implementation Stages’ in the /Specifications folder.
