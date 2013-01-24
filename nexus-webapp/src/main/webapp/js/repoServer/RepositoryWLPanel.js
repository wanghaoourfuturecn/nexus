/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global define*/
define('repoServer/RepositoryWLPanel', ['extjs', 'sonatype/all', 'nexus'], function(Ext, Sonatype, Nexus) {

  var resourceUrl = new Ext.Template(Sonatype.config.repos.urls.repositories + "/{0}/wl").compile();

  var discoveryUpdateIntervalStore = new Ext.data.ArrayStore({
    fields : ['intervalLabel', 'valueHrs'],
    data : [
      ['1 hr', '1'],
      ['2 hr', '2'],
      ['3 hr', '3'],
      ['6 hr', '6'],
      ['9 hr', '9'],
      ['12 hr', '12'],
      ['Daily', '24'],
      ['Weekly', '168']
    ]
  });

  var publishStatusStore = new Ext.data.ArrayStore({
    fields : ['text', 'value'],
    data : [
      ['Not published.', '-1'],
      ['Unknown.', '0'],
      ['Published.', '1']
    ]
  });

  var discoveryStatusStore = new Ext.data.ArrayStore({
    fields : ['text', 'value'],
    data : [
      ['Unsuccessful.', '-1'],
      ['Working...', '0'],
      ['Successful.', '1']
    ]
  });

  Sonatype.repoServer.RepositoryWLPanel = function(cfg) {
    if (!cfg || !cfg.payload) {
      throw new Error("payload missing: need repository record");
    }

    var
          subjectIsNotProxy = cfg.payload.data.repoType !== 'proxy',
          defaultConfig = {
            frame : true,
            autoScroll : true,
            defaultAnchor : '-15',
            readOnly : true, // don't want save/cancel buttons
            url : resourceUrl.apply([cfg.payload.data.id])
          };

    var payload = cfg.payload;

    this.payload = {
      data : {
        id : cfg.payload.data.id,
        resourceURI : resourceUrl.apply([cfg.payload.data.id])
      }
    };

    // don't use payload directly, this panel does not behave as expected by Nexus.ext.FormPanel
    delete cfg.payload;

    Ext.apply(this, cfg, defaultConfig);

    this.dataModifiers = {
      load : {
        'publishedStatus' : function(value) {
          return publishStatusStore.getAt(publishStatusStore.find('value', value)).get('text');
        },

        'discovery.discoveryLastStatus' : function(value) {
          return discoveryStatusStore.getAt(discoveryStatusStore.find('value', value)).get('text');
        }
      }
    };

    this.items = [
      {
        xtype : 'fieldset',
        title : 'Publishing',
        layout : {
          type : 'hbox',
          align : 'stretchmax'
        },
        items : [
          {
            xtype : 'panel',
            layout : 'form',
            flex : 1,
            items : [
              {
                xtype : 'displayfield',
                fieldLabel : 'Status',
                name : 'publishedStatus'
              },
              {
                xtype : 'displayfield',
                fieldLabel : 'Message',
                name : 'publishedMessage'
              },
              {
                xtype : 'timestampDisplayField',
                fieldLabel : 'Published on',
                name : 'publishedTimestamp'
              }
            ]
          },
          {
            xtype : 'container',
            width : 80,
            layout : {
              type : 'vbox',
              pack : 'end'
            },
            items : [
              {
                xtype : 'link-button',
                text : 'Show prefix file',
                href : Sonatype.config.content.repositories + '/' + this.payload.data.id + '/.meta/prefixes.txt',
                target : '_blank'
              }
            ]
          }
        ]
      },
      {
        xtype : 'checkbox',
        fieldLabel : 'Enable Discovery',
        name : 'discovery.discoveryEnabled',
        value : false,
        hidden : subjectIsNotProxy,
        handler : this.enableDiscoveryHandler
      },
      {
        xtype : 'fieldset',
        title : 'Discovery',
        name : 'dis_fieldset',
        hidden : subjectIsNotProxy,
        items : [
          {
            xtype : 'displayfield',
            fieldLabel : 'Status',
            name : 'discovery.discoveryLastStatus'
          },
          {
            xtype : 'displayfield',
            fieldLabel : 'Message',
            name : 'discovery.discoveryLastMessage'
          },
          {
            xtype : 'timestampDisplayField',
            fieldLabel : 'Last run',
            name : 'discovery.discoveryLastRunTimestamp'
          },
          {
            xtype : 'combo',
            fieldLabel : 'Update interval',
            name : 'discovery.discoveryInterval',
            store : discoveryUpdateIntervalStore,
            displayField : 'intervalLabel',
            valueField : 'valueHrs',
            emptyText : 'Select...',
            getListParent : function() {
              return this.el.up('.x-menu');
            },
            iconCls : 'no-icon', //use iconCls if placing within menu to shift to right side of menu
            mode : 'local',
            editable : false,
            allowBlank : false,
            selectOnFocus : true,
            forceSelection : true,
            triggerAction : 'all',
            typeAhead : true,
            listeners : {
              select : function(combo, record, index) {
                Ext.Ajax.request({

                });

              }
            },
            width : 150
          },
          {
            xtype : 'button',
            hideLabel : true,
            text : 'Force remote discovery',
            handler : this.forceRemoteDiscoveryHandler,
            scope : this
          }
        ]
      }
    ];

    Sonatype.repoServer.RepositoryWLPanel.superclass.constructor.call(this);
  };

  Ext.extend(Sonatype.repoServer.RepositoryWLPanel, Nexus.ext.FormPanel, {

    enableDiscoveryHandler : function(checkbox, checked) {
      // 'this' is the checkbox
      var fieldset = this.ownerCt.find('name', 'dis_fieldset');
      if (fieldset.length > 0) {
        fieldset[0].setVisible(checked);
      } else {
        throw new Error('could not find "dis_fieldset"');
      }

    },

    forceRemoteDiscoveryHandler : function(button, event) {
      Ext.Ajax.request({
        method : 'DELETE',
        url : resourceUrl.apply([this.payload.data.id]),
        callback : function() {
          alert('done');
        }
      });
    }
  });

  Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
    var sp = Sonatype.lib.Permissions;

    if (sp.checkPermission('nexus:repositories', sp.CREATE) || sp.checkPermission('nexus:repositories', sp.DELETE)
          || sp.checkPermission('nexus:repositories', sp.EDIT)) {
      cardPanel.add(new Sonatype.repoServer.RepositoryWLPanel({
        tabTitle : 'Whitelist',
        name : 'whitelist',
        payload : rec
      }));
    }
  });
});