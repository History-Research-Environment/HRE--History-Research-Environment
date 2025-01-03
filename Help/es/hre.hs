<?xml ?>

<helpset>

<title>HRE Help System</title>

<wintype default="true">
   <name>help</name>
   <width>35%</width>
   <height>65%</height>
   <x>50%</x>
   <y>30%</y>
   <textfg>#000000</textfg>
   <linkfg>#0000FF</linkfg>
   <bg>#FFFFFF</bg>
   <title>HRE Help Topic</title>
   <toolbar>0e404</toolbar>
</wintype>

<maps>
   <mapref location="hremap.xml"/>
</maps>

<links>
</links>

<view>
   <label>Contents</label>
   <type>oracle.help.navigator.tocNavigator.TOCNavigator</type>
   <data engine="oracle.help.engine.XMLTOCEngine">hretoc.xml</data>
</view>

<view>
   <label>Keywords</label>
   <title>Contents</title>
   <type>oracle.help.navigator.keywordNavigator.KeywordNavigator</type>
   <data engine="oracle.help.engine.XMLIndexEngine">hrekey.xml</data>
</view>

<view>
   <label>Index</label>
   <title>Contents</title>
   <type>oracle.help.navigator.searchNavigator.SearchNavigator</type>
   <data engine="oracle.help.engine.SearchEngine">hre.idx</data>
</view>

</helpset>
