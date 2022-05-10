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
create procedure tpcd..fill_partsupp (in nStartingRow integer, in NumRows integer) {
	
	declare _ps_partkey, _ps_suppkey, _ps_availqty integer;
	declare _ps_comment varchar;
	declare _ps_supplycost numeric(20, 2);
	declare subRow integer;
	
	_ps_partkey := nStartingRow;
	while (_ps_partkey <= NumRows) {
		subRow := 0;
		while (subRow < 4) {
			_ps_suppkey := mod(_ps_partkey + ( subRow * ( 2500 + (_ps_partkey - 1)/10000 ) ), 10000) + 1;
			_ps_availqty := tpcd..randomNumber(1, 9999);
			_ps_supplycost := tpcd..randomNumeric(1, 1000);
			_ps_comment := tpcd..randomString(124, 0, 1);
			
			insert into tpcd..partsupp (ps_partkey, ps_suppkey, ps_availqty, ps_supplycost, ps_comment)  values (_ps_partkey, _ps_suppkey, _ps_availqty, _ps_supplycost, _ps_comment);
			subRow := subRow + 1;
		}
		_ps_partkey := _ps_partkey + 1;
	}
}
