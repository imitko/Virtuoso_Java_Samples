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
create procedure tpcd..fill_customer (in nStartingRow integer, in NumRows integer) {

	declare _c_custkey, _c_nationkey integer;
	declare _c_name, _c_address, _c_phone, _c_mktsegment, _c_comment varchar;
	declare _c_acctbal numeric(20, 2);
	
	_c_custkey := nStartingRow;
	while (_c_custkey <= NumRows) {
		_c_name := concat('Customer', sprintf('%d', _c_custkey));
		_c_address := tpcd..randomString(25, 0, 1);
		_c_nationkey := tpcd..randomNumber(0, 24);
		_c_phone := tpcd..randomPhone(0);
		_c_acctbal := tpcd..randomNumeric(-999.99, 9999.99);
		_c_mktsegment := tpcd..randomSegment(0);
		_c_comment := tpcd..randomString(73, 0, 1);
					
		insert into tpcd..customer (c_custkey, c_name, c_address, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment) values (_c_custkey, _c_name, _c_address, _c_nationkey, _c_phone, _c_acctbal, _c_mktsegment, _c_comment);
		_c_custkey := _c_custkey + 1;
	}
}
