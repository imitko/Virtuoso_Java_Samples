--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..fill_orders (in nStartingGroup integer, in NumGroups integer) {

	declare _o_orderkey, _o_custkey, _o_shippriority integer;
	declare _o_orderstatus, _o_orderpriority, _o_clerk, _o_comment varchar;
	declare _o_totalprice numeric(20, 2);
	declare _o_orderdate date;
	
	declare currentGroup, groupIndex, helper1 integer;
	declare startdate, enddate date;
	
	startdate := stringdate('1992.01.01');
	enddate := stringdate('1998.12.31');
	currentGroup := nStartingGroup;

	while (currentGroup <= NumGroups) {
		groupIndex := 0;
		while (groupIndex < 8) {
			_o_orderkey := (currentGroup - 1) * 32 + 1 + groupIndex;
			_o_custkey := tpcd..randomNumber(1, 150000);
			while (mod(_o_custkey, 3) = 0)
				_o_custkey := tpcd..randomNumber(1, 150000);
			_o_orderdate := 
					dateadd('day', 
						tpcd..randomNumber(0, 
							datediff('day', 
								startdate, 
								dateadd('day', -151, enddate)
							)
						), 
						startdate
					);
			_o_orderpriority := tpcd..randomPriority(0);
			_o_clerk := concat('Clerk', sprintf('%d', tpcd..randomNumber(1, 1000)));
			_o_shippriority := 0;
			_o_comment := tpcd..randomString(49, 0, 1);

			tpcd..fill_lineitems_for_order(_o_orderkey, _o_orderdate, _o_orderstatus, _o_totalprice);
			
			insert into tpcd..orders (o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate, o_orderpriority, o_clerk, o_shippriority, o_comment) values (_o_orderkey, _o_custkey, _o_orderstatus, _o_totalprice, _o_orderdate, _o_orderpriority, _o_clerk, _o_shippriority, _o_comment);
			
			groupIndex := groupIndex + 1;
		}
		currentGroup := currentGroup + 1;
	}
}
